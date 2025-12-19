package com.seele.game.enums;

/**
 * AI难度枚举
 */
public enum AIDifficulty {
    EASY("简单", 0.8),
    MEDIUM("中等", 1.0),
    HARD("困难", 1.2);

    private final String description;
    private final double levelMultiplier; // 等级倍率

    AIDifficulty(String description, double levelMultiplier) {
        this.description = description;
        this.levelMultiplier = levelMultiplier;
    }

    public String getDescription() {
        return description;
    }

    public double getLevelMultiplier() {
        return levelMultiplier;
    }
}
