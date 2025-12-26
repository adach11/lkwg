package com.seele.game.websocket.message;

import lombok.Data;

/**
 * 技能选择消息
 */
@Data
public class SkillSelectMessage {
    /**
     * 战斗ID
     */
    private String battleId;

    /**
     * 玩家ID
     */
    private Long playerId;

    /**
     * 技能ID
     */
    private Long skillId;
}
