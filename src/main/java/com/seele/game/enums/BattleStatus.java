package com.seele.game.enums;

/**
 * 战斗状态枚举
 */
public enum BattleStatus {
    PREPARING("准备中"),
    ONGOING("进行中"),
    FINISHED("已结束");

    private final String description;

    BattleStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
