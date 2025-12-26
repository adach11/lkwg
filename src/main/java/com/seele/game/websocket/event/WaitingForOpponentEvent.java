package com.seele.game.websocket.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 等待对手选择技能事件
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WaitingForOpponentEvent {
    /**
     * 战斗ID
     */
    private String battleId;

    /**
     * 你已选择的技能名称
     */
    private String yourSkillName;

    /**
     * 消息
     */
    private String message;
}
