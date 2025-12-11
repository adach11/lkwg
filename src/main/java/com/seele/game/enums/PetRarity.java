package com.seele.game.enums;

/**
 * 宠物稀有度
 */
public enum PetRarity {
    COMMON(1, "普通"),
    UNCOMMON(2, "优秀"),
    RARE(3, "稀有"),
    EPIC(4, "史诗"),
    LEGENDARY(5, "传说");

    private final int star;
    private final String displayName;

    PetRarity(int star, String displayName) {
        this.star = star;
        this.displayName = displayName;
    }

    public int getStar() {
        return star;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * 根据星级获取稀有度
     */
    public static PetRarity fromStar(int star) {
        for (PetRarity rarity : values()) {
            if (rarity.star == star) {
                return rarity;
            }
        }
        return COMMON;
    }
}
