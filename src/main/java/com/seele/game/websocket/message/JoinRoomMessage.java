package com.seele.game.websocket.message;

import lombok.Data;

/**
 * 加入房间消息
 */
@Data
public class JoinRoomMessage {
    /**
     * 房间ID
     */
    private String roomId;

    /**
     * 玩家ID
     */
    private Long playerId;

    /**
     * 宠物ID
     */
    private Long petId;
}
