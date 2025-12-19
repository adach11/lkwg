package com.seele.game.service.impl;

import com.seele.game.battle.*;
import com.seele.game.dto.BattleEndResponse;
import com.seele.game.entity.PlayerPet;
import com.seele.game.entity.PlayerPetSkill;
import com.seele.game.entity.Skill;
import com.seele.game.enums.AIDifficulty;
import com.seele.game.enums.BattleStatus;
import com.seele.game.enums.PetStatus;
import com.seele.game.mapper.PlayerPetMapper;
import com.seele.game.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 战斗服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BattleServiceImpl implements IBattleService {

    private final PlayerPetMapper playerPetMapper;
    private final ISkillLearnService skillLearnService;
    private final IPetGrowthService petGrowthService;
    private final IAIService aiService;
    private final IBattleCalculationService calculationService;

    // 内存存储战斗状态
    private final ConcurrentHashMap<String, BattleState> battles = new ConcurrentHashMap<>();

    @Override
    public BattleState createPveBattle(Long playerId, Long petId, AIDifficulty difficulty) {
        log.info("创建人机对战: playerId={}, petId={}, difficulty={}", playerId, petId, difficulty);

        // 获取玩家宠物（带技能）
        PlayerPet playerPet = playerPetMapper.selectById(petId);
        if (playerPet == null) {
            throw new RuntimeException("宠物不存在: " + petId);
        }

        if (!playerPet.getPlayerId().equals(playerId)) {
            throw new RuntimeException("宠物不属于该玩家");
        }

        if (playerPet.isFainted()) {
            throw new RuntimeException("宠物已濒死，请先治疗");
        }

        // 获取已装备的技能
        List<PlayerPetSkill> playerSkills = skillLearnService.getEquippedSkills(petId);
        if (playerSkills.isEmpty()) {
            throw new RuntimeException("宠物没有装备任何技能");
        }
        playerPet.setSkills(playerSkills);

        // 生成AI宠物（包含技能）
        PlayerPet aiPet = aiService.generateAIPet(playerPet, difficulty);

        if (aiPet.getSkills() == null || aiPet.getSkills().isEmpty()) {
            throw new RuntimeException("AI宠物技能初始化失败");
        }

        // 创建战斗状态
        BattleState battleState = new BattleState();
        battleState.setBattleId(UUID.randomUUID().toString());
        battleState.setPlayerId(playerId);
        battleState.setStatus(BattleStatus.ONGOING);

        // 创建AI训练师
        AITrainer trainer = new AITrainer();
        trainer.setName(AITrainer.generateTrainerName(difficulty));
        trainer.setDifficulty(difficulty);
        trainer.setPet(aiPet);
        battleState.setAiTrainer(trainer);

        // 初始化双方宠物状态
        battleState.setPlayerPet(BattlePetState.fromPlayerPet(playerPet, playerSkills));
        battleState.setAiPet(BattlePetState.fromPlayerPet(aiPet, aiPet.getSkills()));

        // 保存到内存
        battles.put(battleState.getBattleId(), battleState);

        log.info("战斗创建成功: battleId={}", battleState.getBattleId());
        return battleState;
    }

    @Override
    public BattleState getBattleState(String battleId) {
        BattleState state = battles.get(battleId);
        if (state == null) {
            throw new RuntimeException("战斗不存在或已结束: " + battleId);
        }
        return state;
    }

    @Override
    public BattleRoundResult executeRound(String battleId, Long playerId, Long playerSkillId) {
        BattleState battle = getBattleState(battleId);

        if (battle.isEnded()) {
            throw new RuntimeException("战斗已结束");
        }

        // 验证玩家权限
        if (!battle.getPlayerId().equals(playerId)) {
            throw new RuntimeException("无权操作此战斗");
        }

        battle.setCurrentTurn(battle.getCurrentTurn() + 1);
        log.info("执行第{}回合: battleId={}", battle.getCurrentTurn(), battleId);

        BattleRoundResult roundResult = new BattleRoundResult();
        roundResult.setTurn(battle.getCurrentTurn());

        // 玩家选择技能
        Skill playerSkill = getSkillById(battle.getPlayerPet(), playerSkillId);
        if (playerSkill == null) {
            throw new RuntimeException("技能不存在: " + playerSkillId);
        }

        // AI选择技能
        Skill aiSkill = aiService.selectSkill(battle);

        // 判断先后手
        int playerSpeed = calculationService.getEffectiveSpeed(battle.getPlayerPet());
        int aiSpeed = calculationService.getEffectiveSpeed(battle.getAiPet());

        boolean playerFirst = playerSpeed >= aiSpeed;

        // 执行先手
        BattleActionResult firstAction;
        BattleActionResult secondAction = null;

        if (playerFirst) {
            firstAction = executeAction(battle.getPlayerPet(), battle.getAiPet(), playerSkill);
            roundResult.setFirstAction(firstAction);

            if (!battle.getAiPet().isFainted()) {
                secondAction = executeAction(battle.getAiPet(), battle.getPlayerPet(), aiSkill);
                roundResult.setSecondAction(secondAction);
            }
        } else {
            firstAction = executeAction(battle.getAiPet(), battle.getPlayerPet(), aiSkill);
            roundResult.setFirstAction(firstAction);

            if (!battle.getPlayerPet().isFainted()) {
                secondAction = executeAction(battle.getPlayerPet(), battle.getAiPet(), playerSkill);
                roundResult.setSecondAction(secondAction);
            }
        }

        // 回合结束处理（状态异常伤害）
        processEndOfTurn(battle, roundResult);

        // 检查战斗是否结束
        if (battle.getPlayerPet().isFainted() || battle.getAiPet().isFainted()) {
            String winner = battle.getPlayerPet().isFainted() ? "AI" : "玩家";
            battle.endBattle(winner);
            roundResult.setBattleEnded(true);
            roundResult.setWinner(winner);

            // 如果玩家获胜，结算经验
            if ("玩家".equals(winner)) {
                settleBattleReward(battle);
            }
        }

        // 保存回合结果
        battle.addRoundResult(roundResult);

        return roundResult;
    }

    @Override
    @Transactional
    public BattleEndResponse endBattle(String battleId) {
        BattleState battle = getBattleState(battleId);

        if (!battle.isEnded()) {
            battle.endBattle("AI"); // 投降视为AI胜利
        }

        BattleEndResponse response = BattleEndResponse.builder()
                .battleId(battleId)
                .winner(battle.getWinner())
                .playerWon("玩家".equals(battle.getWinner()))
                .expGained(0)
                .leveledUp(false)
                .message("战斗结束")
                .build();

        // 从内存删除
        battles.remove(battleId);

        return response;
    }

    @Scheduled(fixedRate = 300000) // 每5分钟执行一次
    @Override
    public void cleanupInactiveBattles() {
        int removed = 0;
        for (var entry : battles.entrySet()) {
            if (entry.getValue().isInactive(30)) {
                battles.remove(entry.getKey());
                removed++;
            }
        }
        if (removed > 0) {
            log.info("清理了{}个超时战斗", removed);
        }
    }

    /**
     * 执行单次行动
     */
    private BattleActionResult executeAction(BattlePetState attacker, BattlePetState defender, Skill skill) {
        BattleActionResult result = new BattleActionResult();
        result.setActorName(attacker.getOriginalPet().getNickname());
        result.setSkillName(skill.getName());

        // 检查是否可以行动（状态异常）
        String canActMessage = calculationService.checkCanAct(attacker);
        if (canActMessage != null) {
            result.setHit(false);
            result.setMessage(canActMessage);
            return result;
        }

        // 消耗PP
        if (!attacker.consumePP(skill.getId())) {
            result.setHit(false);
            result.setMessage(String.format("%s的%s没有PP了！", attacker.getOriginalPet().getNickname(), skill.getName()));
            return result;
        }

        // 命中判定
        boolean hit = calculationService.checkHit(skill);
        result.setHit(hit);

        if (!hit) {
            result.setMessage(String.format("%s使用了%s，但是没有命中！", attacker.getOriginalPet().getNickname(), skill.getName()));
            return result;
        }

        // 暴击判定
        boolean critical = calculationService.checkCritical();
        result.setCritical(critical);

        // 计算伤害
        int damage = calculationService.calculateDamage(attacker, defender, skill);
        double effectiveness = calculationService.getTypeEffectiveness(attacker, defender, skill);
        result.setDamage(damage);
        result.setTypeEffectiveness(effectiveness);

        // 应用伤害
        defender.takeDamage(damage);
        result.setTargetFainted(defender.isFainted());

        // 构建消息
        StringBuilder message = new StringBuilder();
        message.append(String.format("%s使用了%s！", attacker.getOriginalPet().getNickname(), skill.getName()));

        if (critical) {
            message.append("暴击！");
        }

        if (effectiveness > 1.0) {
            message.append("效果拔群！");
        } else if (effectiveness > 0 && effectiveness < 1.0) {
            message.append("效果不理想...");
        } else if (effectiveness == 0) {
            message.append("对方免疫了攻击！");
        }

        message.append(String.format("造成了%d点伤害！", damage));

        if (defender.isFainted()) {
            message.append(String.format("%s倒下了！", defender.getOriginalPet().getNickname()));
        }

        result.setMessage(message.toString());

        return result;
    }

    /**
     * 回合结束处理
     */
    private void processEndOfTurn(BattleState battle, BattleRoundResult roundResult) {
        // 处理玩家宠物状态
        String playerStatusMessage = calculationService.processEndOfTurnStatus(battle.getPlayerPet());
        if (playerStatusMessage != null) {
            roundResult.addStatusMessage(playerStatusMessage);
        }

        // 处理AI宠物状态
        String aiStatusMessage = calculationService.processEndOfTurnStatus(battle.getAiPet());
        if (aiStatusMessage != null) {
            roundResult.addStatusMessage(aiStatusMessage);
        }
    }

    /**
     * 结算战斗奖励
     */
    @Transactional
    public void settleBattleReward(BattleState battle) {
        try {
            PlayerPet playerPet = battle.getPlayerPet().getOriginalPet();
            PlayerPet aiPet = battle.getAiPet().getOriginalPet();

            // 计算经验值（基于AI宠物等级）
            int expGained = aiPet.getLevel() * 50;

            log.info("战斗胜利，宠物{}获得{}经验", playerPet.getId(), expGained);

            // 添加经验
            petGrowthService.addExp(playerPetMapper.selectById(playerPet.getId()), expGained);

        } catch (Exception e) {
            log.error("结算战斗奖励失败", e);
        }
    }

    /**
     * 根据ID获取技能
     */
    private Skill getSkillById(BattlePetState pet, Long skillId) {
        return pet.getOriginalPet().getSkills().stream()
                .filter(s -> s.getSkill().getId().equals(skillId))
                .map(PlayerPetSkill::getSkill)
                .findFirst()
                .orElse(null);
    }
}
