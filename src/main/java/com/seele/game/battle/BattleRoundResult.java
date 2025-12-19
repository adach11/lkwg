package com.seele.game.battle;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 回合结果
 */
@Data
@Builder
@AllArgsConstructor
public class BattleRoundResult {
    // 回合数
    private int turn;

    // 先手行动结果
    private BattleActionResult firstAction;

    // 后手行动结果（可能为null，如果先手击败对方）
    private BattleActionResult secondAction;

    // 回合结束时的状态伤害消息
    private List<String> statusMessages;

    // 战斗是否结束
    private boolean battleEnded;

    // 胜者（如果战斗结束）
    private String winner;

    public BattleRoundResult() {
        this.statusMessages = new ArrayList<>();
    }

    public void addStatusMessage(String message) {
        if (statusMessages == null) {
            statusMessages = new ArrayList<>();
        }
        statusMessages.add(message);
    }
}
