package com.seele.game.battle;

import com.seele.game.entity.PlayerPet;
import com.seele.game.enums.AIDifficulty;
import lombok.Data;

/**
 * AI训练师（运行时生成，不持久化）
 */
@Data
public class AITrainer {
    private String name;
    private AIDifficulty difficulty;
    private PlayerPet pet;

    /**
     * 根据难度生成训练师名字
     */
    public static String generateTrainerName(AIDifficulty difficulty) {
        return switch (difficulty) {
            case EASY -> "新手训练师";
            case MEDIUM -> "高级训练师";
            case HARD -> "精英训练师";
        };
    }
}
