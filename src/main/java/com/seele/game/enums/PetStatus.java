package com.seele.game.enums;

/**
 * 宠物异常状态
 */
public enum PetStatus {
    NORMAL("正常"),
    BURN("烧伤"),      // 每回合损失最大HP的1/8，物攻减半
    POISON("中毒"),    // 每回合损失最大HP的1/8
    PARALYSIS("麻痹"), // 速度减半，25%几率无法行动
    FREEZE("冰冻"),    // 无法行动，每回合20%几率解除
    SLEEP("睡眠");     // 无法行动，1-3回合后解除

    private final String displayName;

    PetStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * 判断是否无法行动
     */
    public boolean cannotMove() {
        return this == FREEZE || this == SLEEP;
    }

    /**
     * 判断是否造成持续伤害
     */
    public boolean dealsDamageOverTime() {
        return this == BURN || this == POISON;
    }
}
