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

import java.util.ArrayList;
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
        battleState.setBattleType(com.seele.game.enums.BattleType.PVE);
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

        // 暴击判定（只判定一次）
        boolean critical = calculationService.checkCritical();
        result.setCritical(critical);

        // 计算伤害（传入暴击判定结果，确保一致性）
        int damage = calculationService.calculateDamage(attacker, defender, skill, critical);
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

    @Override
    public BattleState createPvpBattle(Long player1Id, Long player1PetId, Long player2Id, Long player2PetId) {
        log.info("创建PVP对战: player1Id={}, player1PetId={}, player2Id={}, player2PetId={}",
                player1Id, player1PetId, player2Id, player2PetId);

        // 获取玩家1宠物（带技能）
        PlayerPet player1Pet = playerPetMapper.selectById(player1PetId);
        if (player1Pet == null) {
            throw new RuntimeException("玩家1宠物不存在: " + player1PetId);
        }
        if (!player1Pet.getPlayerId().equals(player1Id)) {
            throw new RuntimeException("宠物不属于玩家1");
        }
        if (player1Pet.isFainted()) {
            throw new RuntimeException("玩家1宠物已濒死，请先治疗");
        }

        // 获取玩家1已装备的技能
        List<PlayerPetSkill> player1Skills = skillLearnService.getEquippedSkills(player1PetId);
        if (player1Skills.isEmpty()) {
            throw new RuntimeException("玩家1宠物没有装备任何技能");
        }
        player1Pet.setSkills(player1Skills);

        // 获取玩家2宠物（带技能）
        PlayerPet player2Pet = playerPetMapper.selectById(player2PetId);
        if (player2Pet == null) {
            throw new RuntimeException("玩家2宠物不存在: " + player2PetId);
        }
        if (!player2Pet.getPlayerId().equals(player2Id)) {
            throw new RuntimeException("宠物不属于玩家2");
        }
        if (player2Pet.isFainted()) {
            throw new RuntimeException("玩家2宠物已濒死，请先治疗");
        }

        // 获取玩家2已装备的技能
        List<PlayerPetSkill> player2Skills = skillLearnService.getEquippedSkills(player2PetId);
        if (player2Skills.isEmpty()) {
            throw new RuntimeException("玩家2宠物没有装备任何技能");
        }
        player2Pet.setSkills(player2Skills);

        // 创建战斗状态
        BattleState battleState = new BattleState();
        battleState.setBattleId(UUID.randomUUID().toString());
        battleState.setBattleType(com.seele.game.enums.BattleType.PVP);
        battleState.setPlayer1Id(player1Id);
        battleState.setPlayer2Id(player2Id);
        battleState.setStatus(BattleStatus.ONGOING);

        // 初始化双方宠物状态
        battleState.setPlayer1Pet(BattlePetState.fromPlayerPet(player1Pet, player1Skills));
        battleState.setPlayer2Pet(BattlePetState.fromPlayerPet(player2Pet, player2Skills));

        // 保存到内存
        battles.put(battleState.getBattleId(), battleState);

        log.info("PVP战斗创建成功: battleId={}", battleState.getBattleId());
        return battleState;
    }

    @Override
    public boolean submitSkillChoice(String battleId, Long playerId, Long skillId) {
        BattleState battle = getBattleState(battleId);

        if (!battle.isPvp()) {
            throw new RuntimeException("这不是PVP战斗");
        }

        if (battle.isEnded()) {
            throw new RuntimeException("战斗已结束");
        }

        // 验证玩家权限
        boolean isPlayer1 = battle.getPlayer1Id().equals(playerId);
        boolean isPlayer2 = battle.getPlayer2Id().equals(playerId);

        if (!isPlayer1 && !isPlayer2) {
            throw new RuntimeException("你不在此战斗中");
        }

        // 验证技能
        BattlePetState playerPet = isPlayer1 ? battle.getPlayer1Pet() : battle.getPlayer2Pet();
        Skill skill = getSkillById(playerPet, skillId);
        if (skill == null) {
            throw new RuntimeException("技能不存在: " + skillId);
        }

        // 提交技能选择
        if (isPlayer1) {
            battle.setPlayer1SkillChoice(skillId);
            battle.setPlayer1Ready(true);
            log.info("玩家1已选择技能: battleId={}, skillId={}", battleId, skillId);
        } else {
            battle.setPlayer2SkillChoice(skillId);
            battle.setPlayer2Ready(true);
            log.info("玩家2已选择技能: battleId={}, skillId={}", battleId, skillId);
        }

        // 返回是否双方都已准备好
        return battle.bothPlayersReady();
    }

    @Override
    public BattleRoundResult executePvpRound(String battleId) {
        BattleState battle = getBattleState(battleId);

        if (!battle.isPvp()) {
            throw new RuntimeException("这不是PVP战斗");
        }

        if (battle.isEnded()) {
            throw new RuntimeException("战斗已结束");
        }

        if (!battle.bothPlayersReady()) {
            throw new RuntimeException("双方玩家尚未都选择技能");
        }

        battle.setCurrentTurn(battle.getCurrentTurn() + 1);
        log.info("执行PVP第{}回合: battleId={}", battle.getCurrentTurn(), battleId);

        BattleRoundResult roundResult = new BattleRoundResult();
        roundResult.setTurn(battle.getCurrentTurn());

        // 获取双方选择的技能
        Skill player1Skill = getSkillById(battle.getPlayer1Pet(), battle.getPlayer1SkillChoice());
        Skill player2Skill = getSkillById(battle.getPlayer2Pet(), battle.getPlayer2SkillChoice());

        if (player1Skill == null || player2Skill == null) {
            throw new RuntimeException("技能选择无效");
        }

        // 判断先后手
        int player1Speed = calculationService.getEffectiveSpeed(battle.getPlayer1Pet());
        int player2Speed = calculationService.getEffectiveSpeed(battle.getPlayer2Pet());

        boolean player1First = player1Speed >= player2Speed;

        // 执行先手
        BattleActionResult firstAction;
        BattleActionResult secondAction = null;

        if (player1First) {
            firstAction = executeAction(battle.getPlayer1Pet(), battle.getPlayer2Pet(), player1Skill);
            roundResult.setFirstAction(firstAction);

            if (!battle.getPlayer2Pet().isFainted()) {
                secondAction = executeAction(battle.getPlayer2Pet(), battle.getPlayer1Pet(), player2Skill);
                roundResult.setSecondAction(secondAction);
            }
        } else {
            firstAction = executeAction(battle.getPlayer2Pet(), battle.getPlayer1Pet(), player2Skill);
            roundResult.setFirstAction(firstAction);

            if (!battle.getPlayer1Pet().isFainted()) {
                secondAction = executeAction(battle.getPlayer1Pet(), battle.getPlayer2Pet(), player1Skill);
                roundResult.setSecondAction(secondAction);
            }
        }

        // 回合结束处理（状态异常伤害）
        processEndOfTurn(battle, roundResult);

        // 检查战斗是否结束
        if (battle.getPlayer1Pet().isFainted() || battle.getPlayer2Pet().isFainted()) {
            String winner;
            if (battle.getPlayer1Pet().isFainted() && battle.getPlayer2Pet().isFainted()) {
                winner = "平局";
            } else if (battle.getPlayer1Pet().isFainted()) {
                winner = "玩家2";
            } else {
                winner = "玩家1";
            }

            battle.endBattle(winner);
            roundResult.setBattleEnded(true);
            roundResult.setWinner(winner);

            // PVP战斗结束，双方都获得一定经验
            settlePvpBattleReward(battle);
        } else {
            // 重置技能选择，准备下一回合
            battle.resetSkillChoices();
        }

        // 保存回合结果
        battle.addRoundResult(roundResult);

        return roundResult;
    }

    /**
     * 结算PVP战斗奖励
     */
    @Transactional
    public void settlePvpBattleReward(BattleState battle) {
        try {
            PlayerPet player1Pet = battle.getPlayer1Pet().getOriginalPet();
            PlayerPet player2Pet = battle.getPlayer2Pet().getOriginalPet();

            // 计算经验值
            int player1Exp = player2Pet.getLevel() * 30; // 基于对手等级
            int player2Exp = player1Pet.getLevel() * 30;

            // 胜者额外获得50%经验
            if ("玩家1".equals(battle.getWinner())) {
                player1Exp = (int) (player1Exp * 1.5);
            } else if ("玩家2".equals(battle.getWinner())) {
                player2Exp = (int) (player2Exp * 1.5);
            }

            log.info("PVP战斗结束，玩家1宠物{}获得{}经验，玩家2宠物{}获得{}经验",
                    player1Pet.getId(), player1Exp, player2Pet.getId(), player2Exp);

            // 添加经验
            if (player1Exp > 0) {
                petGrowthService.addExp(playerPetMapper.selectById(player1Pet.getId()), player1Exp);
            }
            if (player2Exp > 0) {
                petGrowthService.addExp(playerPetMapper.selectById(player2Pet.getId()), player2Exp);
            }

        } catch (Exception e) {
            log.error("结算PVP战斗奖励失败", e);
        }
    }

    @Override
    public BattleState createTeamPveBattle(Long playerId, Long[] petIds, AIDifficulty difficulty) {
        log.info("创建队伍PVE战斗: playerId={}, petIds={}, difficulty={}", playerId, petIds, difficulty);

        if (petIds == null || petIds.length == 0 || petIds.length > 6) {
            throw new RuntimeException("宠物队伍数量必须在1-6之间");
        }

        // 加载玩家的宠物队伍
        List<BattlePetState> playerTeam = new ArrayList<>();
        for (Long petId : petIds) {
            PlayerPet pet = playerPetMapper.selectById(petId);
            if (pet == null) {
                throw new RuntimeException("宠物不存在: " + petId);
            }
            if (!pet.getPlayerId().equals(playerId)) {
                throw new RuntimeException("宠物不属于该玩家: " + petId);
            }
            if (pet.isFainted()) {
                throw new RuntimeException("宠物已濒死，请先治疗: " + pet.getNickname());
            }

            List<PlayerPetSkill> skills = skillLearnService.getEquippedSkills(petId);
            if (skills.isEmpty()) {
                throw new RuntimeException("宠物没有装备任何技能: " + pet.getNickname());
            }
            pet.setSkills(skills);
            playerTeam.add(BattlePetState.fromPlayerPet(pet, skills));
        }

        // 生成AI队伍（基于玩家第一只宠物的等级）
        List<BattlePetState> aiTeam = new ArrayList<>();
        PlayerPet firstPet = playerTeam.get(0).getOriginalPet();
        for (int i = 0; i < petIds.length; i++) {
            PlayerPet aiPet = aiService.generateAIPet(firstPet, difficulty);
            aiTeam.add(BattlePetState.fromPlayerPet(aiPet, aiPet.getSkills()));
        }

        // 创建战斗状态
        BattleState battleState = new BattleState();
        battleState.setBattleId(UUID.randomUUID().toString());
        battleState.setBattleType(com.seele.game.enums.BattleType.PVE);
        battleState.setPlayer1Id(playerId);
        battleState.setStatus(BattleStatus.ONGOING);

        // 设置队伍模式
        battleState.setPlayer1Team(playerTeam);
        battleState.setPlayer2Team(aiTeam);
        battleState.setPlayer1ActiveIndex(0);
        battleState.setPlayer2ActiveIndex(0);

        // 为了兼容性，也设置单宠物字段
        battleState.setPlayer1Pet(playerTeam.get(0));
        battleState.setPlayer2Pet(aiTeam.get(0));

        // 创建AI训练师
        AITrainer trainer = new AITrainer();
        trainer.setName(AITrainer.generateTrainerName(difficulty));
        trainer.setDifficulty(difficulty);
        trainer.setPet(aiTeam.get(0).getOriginalPet());
        battleState.setAiTrainer(trainer);

        battles.put(battleState.getBattleId(), battleState);

        log.info("队伍PVE战斗创建成功: battleId={}, 队伍大小={}", battleState.getBattleId(), petIds.length);
        return battleState;
    }

    @Override
    public BattleState createTeamPvpBattle(Long player1Id, Long[] player1PetIds, Long player2Id, Long[] player2PetIds) {
        log.info("创建队伍PVP战斗: player1Id={}, player2Id={}", player1Id, player2Id);

        if (player1PetIds == null || player1PetIds.length == 0 || player1PetIds.length > 6) {
            throw new RuntimeException("玩家1宠物队伍数量必须在1-6之间");
        }
        if (player2PetIds == null || player2PetIds.length == 0 || player2PetIds.length > 6) {
            throw new RuntimeException("玩家2宠物队伍数量必须在1-6之间");
        }

        // 加载玩家1的宠物队伍
        List<BattlePetState> player1Team = new ArrayList<>();
        for (Long petId : player1PetIds) {
            PlayerPet pet = playerPetMapper.selectById(petId);
            if (pet == null) {
                throw new RuntimeException("玩家1宠物不存在: " + petId);
            }
            if (!pet.getPlayerId().equals(player1Id)) {
                throw new RuntimeException("宠物不属于玩家1: " + petId);
            }
            if (pet.isFainted()) {
                throw new RuntimeException("玩家1宠物已濒死: " + pet.getNickname());
            }

            List<PlayerPetSkill> skills = skillLearnService.getEquippedSkills(petId);
            if (skills.isEmpty()) {
                throw new RuntimeException("玩家1宠物没有装备技能: " + pet.getNickname());
            }
            pet.setSkills(skills);
            player1Team.add(BattlePetState.fromPlayerPet(pet, skills));
        }

        // 加载玩家2的宠物队伍
        List<BattlePetState> player2Team = new ArrayList<>();
        for (Long petId : player2PetIds) {
            PlayerPet pet = playerPetMapper.selectById(petId);
            if (pet == null) {
                throw new RuntimeException("玩家2宠物不存在: " + petId);
            }
            if (!pet.getPlayerId().equals(player2Id)) {
                throw new RuntimeException("宠物不属于玩家2: " + petId);
            }
            if (pet.isFainted()) {
                throw new RuntimeException("玩家2宠物已濒死: " + pet.getNickname());
            }

            List<PlayerPetSkill> skills = skillLearnService.getEquippedSkills(petId);
            if (skills.isEmpty()) {
                throw new RuntimeException("玩家2宠物没有装备技能: " + pet.getNickname());
            }
            pet.setSkills(skills);
            player2Team.add(BattlePetState.fromPlayerPet(pet, skills));
        }

        // 创建战斗状态
        BattleState battleState = new BattleState();
        battleState.setBattleId(UUID.randomUUID().toString());
        battleState.setBattleType(com.seele.game.enums.BattleType.PVP);
        battleState.setPlayer1Id(player1Id);
        battleState.setPlayer2Id(player2Id);
        battleState.setStatus(BattleStatus.ONGOING);

        // 设置队伍模式
        battleState.setPlayer1Team(player1Team);
        battleState.setPlayer2Team(player2Team);
        battleState.setPlayer1ActiveIndex(0);
        battleState.setPlayer2ActiveIndex(0);

        // 为了兼容性，也设置单宠物字段
        battleState.setPlayer1Pet(player1Team.get(0));
        battleState.setPlayer2Pet(player2Team.get(0));

        battles.put(battleState.getBattleId(), battleState);

        log.info("队伍PVP战斗创建成功: battleId={}", battleState.getBattleId());
        return battleState;
    }

    @Override
    public boolean submitSwitchPet(String battleId, Long playerId, int switchToIndex) {
        BattleState battle = getBattleState(battleId);

        if (battle.isEnded()) {
            throw new RuntimeException("战斗已结束");
        }

        if (!battle.isTeamMode()) {
            throw new RuntimeException("此战斗不是队伍模式，无法切换宠物");
        }

        if (switchToIndex < 0 || switchToIndex >= 6) {
            throw new RuntimeException("宠物索引无效: " + switchToIndex);
        }

        // 验证玩家权限
        boolean isPlayer1 = battle.getPlayer1Id().equals(playerId);
        boolean isPlayer2 = battle.isPvp() && battle.getPlayer2Id().equals(playerId);

        if (!isPlayer1 && !isPlayer2) {
            throw new RuntimeException("你不在此战斗中");
        }

        // 验证切换的宠物是否有效
        List<BattlePetState> team = isPlayer1 ? battle.getPlayer1Team() : battle.getPlayer2Team();
        int currentIndex = isPlayer1 ? battle.getPlayer1ActiveIndex() : battle.getPlayer2ActiveIndex();

        if (switchToIndex >= team.size()) {
            throw new RuntimeException("宠物索引超出队伍范围");
        }

        if (switchToIndex == currentIndex) {
            throw new RuntimeException("该宠物已经在场上");
        }

        BattlePetState targetPet = team.get(switchToIndex);
        if (targetPet.isFainted()) {
            throw new RuntimeException("不能切换到已濒死的宠物");
        }

        // 提交切换选择
        if (isPlayer1) {
            battle.setPlayer1SwitchTo(switchToIndex);
            battle.setPlayer1Ready(true);
            battle.setPlayer1SkillChoice(null);  // 清空技能选择
            log.info("玩家1选择切换到宠物{}: battleId={}", switchToIndex, battleId);
        } else {
            battle.setPlayer2SwitchTo(switchToIndex);
            battle.setPlayer2Ready(true);
            battle.setPlayer2SkillChoice(null);  // 清空技能选择
            log.info("玩家2选择切换到宠物{}: battleId={}", switchToIndex, battleId);
        }

        // PVE模式下直接返回true，PVP模式下检查双方是否都准备好
        return !battle.isPvp() || battle.bothPlayersReady();
    }
}
