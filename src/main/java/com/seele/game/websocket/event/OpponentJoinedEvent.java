package com.seele.game.websocket.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 对手加入事件
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpponentJoinedEvent {
    /**
     * 对手ID
     */
    private Long opponentId;

    /**
     * 对手昵称
     */
    private String opponentName;

    /**
     * 宠物名称
     */
    private String petName;

    /**
     * 宠物等级
     */
    private Integer petLevel;

    /**
     * 消息
     */
    private String message;
}
