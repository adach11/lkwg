package com.seele.game.battle;

import com.seele.game.enums.BattleStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 战斗状态（纯内存对象）
 */
@Data
public class BattleState {
    // 战斗ID
    private String battleId;

    // 玩家ID
    private Long playerId;

    // 战斗状态
    private BattleStatus status;

    // 当前回合数
    private int currentTurn;

    // 双方宠物状态
    private BattlePetState playerPet;
    private BattlePetState aiPet;

    // AI训练师
    private AITrainer aiTrainer;

    // 最近的行动历史（最多保留10回合）
    private List<BattleRoundResult> recentRounds;

    // 战斗开始时间
    private LocalDateTime startTime;

    // 战斗结束时间
    private LocalDateTime endTime;

    // 胜者
    private String winner;

    public BattleState() {
        this.recentRounds = new ArrayList<>();
        this.currentTurn = 0;
        this.status = BattleStatus.PREPARING;
        this.startTime = LocalDateTime.now();
    }

    /**
     * 添加回合结果
     */
    public void addRoundResult(BattleRoundResult result) {
        recentRounds.add(result);
        // 只保留最近10回合
        if (recentRounds.size() > 10) {
            recentRounds.remove(0);
        }
    }

    /**
     * 检查战斗是否超时（30分钟）
     */
    public boolean isInactive(int minutes) {
        if (status == BattleStatus.FINISHED) {
            return false;
        }
        return LocalDateTime.now().isAfter(startTime.plusMinutes(minutes));
    }

    /**
     * 结束战斗
     */
    public void endBattle(String winner) {
        this.status = BattleStatus.FINISHED;
        this.endTime = LocalDateTime.now();
        this.winner = winner;
    }

    /**
     * 检查战斗是否结束
     */
    public boolean isEnded() {
        return status == BattleStatus.FINISHED;
    }
}
