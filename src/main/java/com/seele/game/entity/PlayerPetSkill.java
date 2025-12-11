package com.seele.game.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 玩家宠物已学会的技能
 * 存储玩家宠物已经学会的所有技能以及当前装备的技能
 */
@Entity
@Table(name = "player_pet_skill",
       uniqueConstraints = @UniqueConstraint(columnNames = {"player_pet_id", "skill_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerPetSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 玩家宠物ID
     */
    @Column(nullable = false)
    private Long playerPetId;

    /**
     * 技能ID
     */
    @Column(nullable = false)
    private Long skillId;

    /**
     * 当前PP（剩余使用次数）
     */
    @Column(nullable = false)
    private Integer currentPp;

    /**
     * 最大PP
     */
    @Column(nullable = false)
    private Integer maxPp;

    /**
     * 技能位置 (0-3)，表示装备在哪个技能槽
     * null表示已学会但未装备
     */
    private Integer position;

    /**
     * 是否装备在技能栏
     */
    @Column(nullable = false)
    private Boolean isEquipped = false;

    /**
     * 学会时间
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime learnedAt;

    /**
     * 更新时间
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        learnedAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

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
