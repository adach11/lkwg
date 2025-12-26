package com.seele.game.websocket.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 战斗结束事件
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BattleEndEvent {
    /**
     * 战斗ID
     */
    private String battleId;

    /**
     * 胜者
     */
    private String winner;

    /**
     * 你是否胜利
     */
    private Boolean youWon;

    /**
     * 获得的经验值（胜利时）
     */
    private Integer expGained;

    /**
     * 是否升级
     */
    private Boolean leveledUp;

    /**
     * 消息
     */
    private String message;
}
