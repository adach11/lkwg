package com.seele.game.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 玩家宠物已学会的技能
 * 存储玩家宠物已经学会的所有技能以及当前装备的技能
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerPetSkill {

    private Long id;

    /**
     * 玩家宠物ID
     */
    private Long playerPetId;

    /**
     * 技能ID
     */
    private Long skillId;

    /**
     * 当前PP（剩余使用次数）
     */
    private Integer currentPp;

    /**
     * 最大PP
     */
    private Integer maxPp;

    /**
     * 技能位置 (0-3)，表示装备在哪个技能槽
     * null表示已学会但未装备
     */
    private Integer position;

    /**
     * 是否装备在技能栏
     */
    private Boolean isEquipped;

    /**
     * 学会时间
     */
    private LocalDateTime learnedAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 使用技能（消耗PP）
     */
    public boolean usePp() {
        if (currentPp > 0) {
            currentPp--;
            return true;
        }
        return false;
    }

    /**
     * 恢复PP
     */
    public void restorePp() {
        currentPp = maxPp;
    }
}
