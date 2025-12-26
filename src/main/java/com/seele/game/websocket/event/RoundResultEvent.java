package com.seele.game.websocket.event;

import com.seele.game.battle.BattleRoundResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 回合结果事件
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoundResultEvent {
    /**
     * 战斗ID
     */
    private String battleId;

    /**
     * 回合结果
     */
    private BattleRoundResult roundResult;

    /**
     * 下一回合超时时间（秒）
     */
    private Integer nextTurnTimeout;

    /**
     * 消息
     */
    private String message;
}
