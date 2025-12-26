package com.seele.game.websocket.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 房间创建成功事件
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomCreatedEvent {
    /**
     * 房间ID
     */
    private String roomId;

    /**
     * 创建者ID
     */
    private Long creatorId;

    /**
     * 创建者昵称
     */
    private String creatorName;

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
