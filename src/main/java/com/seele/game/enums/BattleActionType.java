package com.seele.game.enums;

/**
 * 战斗行动类型枚举
 */
public enum BattleActionType {
    USE_SKILL("使用技能"),
    SWITCH_PET("切换宠物"),
    USE_ITEM("使用道具"),
    FLEE("逃跑");

    private final String description;

    BattleActionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
