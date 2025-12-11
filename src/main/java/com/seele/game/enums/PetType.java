package com.seele.game.enums;

/**
 * 宠物属性类型
 */
public enum PetType {
    FIRE("火"),
    WATER("水"),
    GRASS("草"),
    ELECTRIC("电"),
    GROUND("土"),
    DRAGON("龙");

    private final String displayName;

    PetType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * 计算属性相克倍率
     * @param defender 防守方属性
     * @return 伤害倍率：1.5(克制), 0.5(被克), 0.0(免疫), 1.0(普通)
     */
    public double getEffectiveness(PetType defender) {
        // 火 > 草 > 水 > 火 (基础循环)
        if (this == FIRE && defender == GRASS) return 1.5;
        if (this == GRASS && defender == WATER) return 1.5;
        if (this == WATER && defender == FIRE) return 1.5;

        // 反向被克
        if (this == FIRE && defender == WATER) return 0.5;
        if (this == WATER && defender == GRASS) return 0.5;
        if (this == GRASS && defender == FIRE) return 0.5;

        // 电 > 水 (额外克制)
        if (this == ELECTRIC && defender == WATER) return 1.5;

        // 土 > 电 (免疫)
        if (this == ELECTRIC && defender == GROUND) return 0.0;

        // 龙 > 龙 (互克)
        if (this == DRAGON && defender == DRAGON) return 1.5;

        // 默认普通伤害
        return 1.0;
    }
}
