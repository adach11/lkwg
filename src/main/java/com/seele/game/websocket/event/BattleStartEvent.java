package com.seele.game.websocket.event;

import com.seele.game.battle.BattleState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 战斗开始事件
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BattleStartEvent {
    /**
     * 战斗ID
     */
    private String battleId;

    /**
     * 你的玩家编号（1或2）
     */
    private Integer yourPlayerNumber;

    /**
     * 战斗状态快照
     */
    private BattleStateSnapshot battleState;

    /**
     * 消息
     */
    private String message;

    /**
     * 战斗状态快照（简化版，避免发送过多数据）
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BattleStateSnapshot {
        private String battleId;
        private Integer currentTurn;
        private PetSnapshot player1Pet;
        private PetSnapshot player2Pet;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PetSnapshot {
        private String name;
        private Integer level;
        private String type;
        private Integer currentHp;
        private Integer maxHp;
        private String status;
    }
}
