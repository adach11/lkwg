package com.seele.game.websocket.message;

import lombok.Data;

/**
 * 随机匹配消息
 */
@Data
public class MatchmakingMessage {
    /**
     * 玩家ID
     */
    private Long playerId;

    /**
     * 宠物ID
     */
    private Long petId;
}
