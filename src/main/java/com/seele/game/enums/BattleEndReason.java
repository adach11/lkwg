package com.seele.game.enums;

/**
 * 战斗结束原因枚举
 */
public enum BattleEndReason {
    PLAYER_WIN("玩家胜利"),
    AI_WIN("AI胜利"),
    PLAYER_FLED("玩家逃跑"),
    TIMEOUT("超时"),
    ERROR("错误");

    private final String description;

    BattleEndReason(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
