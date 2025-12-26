package com.seele.game.websocket.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 对手断线事件
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpponentDisconnectedEvent {
    /**
     * 战斗ID
     */
    private String battleId;

    /**
     * 断线原因
     */
    private String reason;

    /**
     * 等待重连时间（秒）
     */
    private Integer waitTime;

    /**
     * 消息
     */
    private String message;
}
