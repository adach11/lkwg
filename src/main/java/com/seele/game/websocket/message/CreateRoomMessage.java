package com.seele.game.websocket.message;

import lombok.Data;

/**
 * 创建房间消息
 */
@Data
public class CreateRoomMessage {
    /**
     * 玩家ID
     */
    private Long playerId;

    /**
     * 宠物ID
     */
    private Long petId;
}
