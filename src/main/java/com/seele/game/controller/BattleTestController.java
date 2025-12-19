package com.seele.game.controller;

import com.seele.game.battle.BattleActionResult;
import com.seele.game.battle.BattleRoundResult;
import com.seele.game.battle.BattleState;
import com.seele.game.enums.AIDifficulty;
import com.seele.game.service.IBattleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Random;

/**
 * 战斗测试控制器
 * 提供格式化的文本输出，方便在没有前端的情况下测试战斗系统
 */
@Slf4j
@RestController
@RequestMapping("/api/battle/test")
@RequiredArgsConstructor
public class BattleTestController {

    private final IBattleService battleService;
    private final Random random = new Random();

    /**
     * 自动战斗到结束
     * 返回格式化的战斗日志
     */
    @PostMapping("/auto-battle")
    public String autoBattle(
            @RequestParam Long playerId,
            @RequestParam Long petId,
            @RequestParam(defaultValue = "MEDIUM") AIDifficulty difficulty) {

        StringBuilder log = new StringBuilder();

        try {
            // 创建战斗
            BattleState battle = battleService.createPveBattle(playerId, petId, difficulty);

            log.append("═══════════════════════════════════════\n");
            log.append("            战斗开始！\n");
            log.append("═══════════════════════════════════════\n\n");
            log.append(formatBattleStart(battle));
            log.append("\n");

            // 自动执行到结束
            int maxTurns = 50; // 防止无限循环
            int turn = 0;

            while (!battle.isEnded() && turn < maxTurns) {
                turn++;

                // 随机选择玩家技能
                Long skillId = battle.getPlayerPet().getOriginalPet().getSkills().get(
                        random.nextInt(battle.getPlayerPet().getOriginalPet().getSkills().size())
                ).getSkill().getId();

                // 执行回合
                BattleRoundResult result = battleService.executeRound(battle.getBattleId(), playerId, skillId);

                log.append("───────────────────────────────────────\n");
                log.append(String.format("            第 %d 回合\n", result.getTurn()));
                log.append("───────────────────────────────────────\n\n");
                log.append(formatRoundResult(result));
                log.append("\n");
                log.append(formatCurrentStatus(battle));
                log.append("\n");

                if (result.isBattleEnded()) {
                    break;
                }
            }

            if (turn >= maxTurns) {
                log.append("\n战斗超过最大回合数，强制结束\n");
            }

            log.append("═══════════════════════════════════════\n");
            log.append("            战斗结束！\n");
            log.append("═══════════════════════════════════════\n\n");
            log.append(formatBattleEnd(battle));

        } catch (Exception e) {
            log.append("\n错误: ").append(e.getMessage()).append("\n");
            log.append("堆栈信息:\n");
            for (StackTraceElement element : e.getStackTrace()) {
                log.append("  ").append(element.toString()).append("\n");
            }
        }

        return log.toString();
    }

    /**
     * 手动控制战斗（创建）
     */
    @PostMapping("/manual/start")
    public String manualStart(
            @RequestParam Long playerId,
            @RequestParam Long petId,
            @RequestParam(defaultValue = "MEDIUM") AIDifficulty difficulty) {

        StringBuilder log = new StringBuilder();

        try {
            BattleState battle = battleService.createPveBattle(playerId, petId, difficulty);

            log.append("═══════════════════════════════════════\n");
            log.append("            战斗创建成功！\n");
            log.append("═══════════════════════════════════════\n\n");
            log.append("战斗ID: ").append(battle.getBattleId()).append("\n\n");
            log.append(formatBattleStart(battle));
            log.append("\n\n");
            log.append("使用以下接口执行攻击:\n");
            log.append(String.format("POST /api/battle/test/manual/attack?battleId=%s&skillId=<技能ID>\n", battle.getBattleId()));
            log.append("\n可用技能:\n");
            battle.getPlayerPet().getOriginalPet().getSkills().forEach(skill -> {
                log.append(String.format("  - ID: %d, 名称: %s, 威力: %d, PP: %d\n",
                        skill.getSkill().getId(),
                        skill.getSkill().getName(),
                        skill.getSkill().getPower(),
                        battle.getPlayerPet().getSkillPpMap().get(skill.getSkill().getId())));
            });

        } catch (Exception e) {
            log.append("错误: ").append(e.getMessage()).append("\n");
        }

        return log.toString();
    }

    /**
     * 手动控制战斗（执行回合）
     */
    @PostMapping("/manual/attack")
    public String manualAttack(
            @RequestParam String battleId,
            @RequestParam Long playerId,
            @RequestParam Long skillId) {

        StringBuilder log = new StringBuilder();

        try {
            BattleState battle = battleService.getBattleState(battleId);
            BattleRoundResult result = battleService.executeRound(battleId, playerId, skillId);

            log.append("───────────────────────────────────────\n");
            log.append(String.format("            第 %d 回合\n", result.getTurn()));
            log.append("───────────────────────────────────────\n\n");
            log.append(formatRoundResult(result));
            log.append("\n");
            log.append(formatCurrentStatus(battle));

            if (result.isBattleEnded()) {
                log.append("\n\n═══════════════════════════════════════\n");
                log.append("            战斗结束！\n");
                log.append("═══════════════════════════════════════\n\n");
                log.append(formatBattleEnd(battle));
            }

        } catch (Exception e) {
            log.append("错误: ").append(e.getMessage()).append("\n");
        }

        return log.toString();
    }

    /**
     * 格式化战斗开始信息
     */
    private String formatBattleStart(BattleState battle) {
        return String.format(
                "%s 派出了 %s (Lv.%d)\n" +
                "  ├─ HP: %d/%d\n" +
                "  ├─ 属性: %s\n" +
                "  └─ 状态: %s\n\n" +
                "%s 派出了 %s (Lv.%d)\n" +
                "  ├─ HP: %d/%d\n" +
                "  ├─ 属性: %s\n" +
                "  └─ 状态: %s\n",
                "玩家",
                battle.getPlayerPet().getOriginalPet().getNickname(),
                battle.getPlayerPet().getOriginalPet().getLevel(),
                battle.getPlayerPet().getCurrentHp(),
                battle.getPlayerPet().getMaxHp(),
                battle.getPlayerPet().getOriginalPet().getTemplate().getType().getDisplayName(),
                battle.getPlayerPet().getStatusCondition().name(),
                battle.getAiTrainer().getName(),
                battle.getAiPet().getOriginalPet().getNickname(),
                battle.getAiPet().getOriginalPet().getLevel(),
                battle.getAiPet().getCurrentHp(),
                battle.getAiPet().getMaxHp(),
                battle.getAiPet().getOriginalPet().getTemplate().getType().getDisplayName(),
                battle.getAiPet().getStatusCondition().name()
        );
    }

    /**
     * 格式化回合结果
     */
    private String formatRoundResult(BattleRoundResult result) {
        StringBuilder sb = new StringBuilder();

        // 先手行动
        sb.append(formatAction(result.getFirstAction()));

        // 后手行动
        if (result.getSecondAction() != null) {
            sb.append("\n").append(formatAction(result.getSecondAction()));
        }

        // 状态消息
        if (result.getStatusMessages() != null && !result.getStatusMessages().isEmpty()) {
            sb.append("\n\n[状态效果]\n");
            for (String msg : result.getStatusMessages()) {
                sb.append("  • ").append(msg).append("\n");
            }
        }

        return sb.toString();
    }

    /**
     * 格式化单次行动
     */
    private String formatAction(BattleActionResult action) {
        return String.format("[%s的行动]\n%s",
                action.getActorName(),
                action.getMessage());
    }

    /**
     * 格式化当前状态
     */
    private String formatCurrentStatus(BattleState battle) {
        return String.format(
                "\n[当前状态]\n" +
                "玩家方: %s  HP: %d/%d (%.1f%%)  状态: %s\n" +
                "AI方:   %s  HP: %d/%d (%.1f%%)  状态: %s",
                battle.getPlayerPet().getOriginalPet().getNickname(),
                battle.getPlayerPet().getCurrentHp(),
                battle.getPlayerPet().getMaxHp(),
                (double) battle.getPlayerPet().getCurrentHp() / battle.getPlayerPet().getMaxHp() * 100,
                battle.getPlayerPet().getStatusCondition().name(),
                battle.getAiPet().getOriginalPet().getNickname(),
                battle.getAiPet().getCurrentHp(),
                battle.getAiPet().getMaxHp(),
                (double) battle.getAiPet().getCurrentHp() / battle.getAiPet().getMaxHp() * 100,
                battle.getAiPet().getStatusCondition().name()
        );
    }

    /**
     * 格式化战斗结束
     */
    private String formatBattleEnd(BattleState battle) {
        return String.format(
                "胜者: %s\n\n" +
                "最终状态:\n" +
                "  玩家方: %s  HP: %d/%d\n" +
                "  AI方:   %s  HP: %d/%d\n",
                battle.getWinner(),
                battle.getPlayerPet().getOriginalPet().getNickname(),
                battle.getPlayerPet().getCurrentHp(),
                battle.getPlayerPet().getMaxHp(),
                battle.getAiPet().getOriginalPet().getNickname(),
                battle.getAiPet().getCurrentHp(),
                battle.getAiPet().getMaxHp()
        );
    }
}
