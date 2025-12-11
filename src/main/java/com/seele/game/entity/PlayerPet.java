package com.seele.game.entity;

import com.seele.game.enums.PetStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 玩家宠物实例 - 每个玩家拥有的具体宠物
 */
@Entity
@Table(name = "player_pet")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerPet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 玩家ID
     */
    @Column(nullable = false)
    private Long playerId;

    /**
     * 宠物模板ID
     */
    @Column(nullable = false)
    private Long petTemplateId;

    /**
     * 昵称（玩家自定义）
     */
    @Column(length = 50)
    private String nickname;

    /**
     * 当前等级 (1-100)
     */
    @Column(nullable = false)
    private Integer level = 1;

    /**
     * 当前经验值
     */
    @Column(nullable = false)
    private Integer exp = 0;

    /**
     * 升到下一级所需经验值
     */
    @Column(nullable = false)
    private Integer nextLevelExp;

    /**
     * 个体值 (IV) - 范围 0-31
     * 用于增加宠物属性的随机性和独特性
     */
    @Column(nullable = false)
    private Integer ivHp;

    @Column(nullable = false)
    private Integer ivAttack;

    @Column(nullable = false)
    private Integer ivDefense;

    @Column(nullable = false)
    private Integer ivMagicAttack;

    @Column(nullable = false)
    private Integer ivMagicDefense;

    @Column(nullable = false)
    private Integer ivSpeed;

    /**
     * 当前HP（战斗中会变化）
     */
    @Column(nullable = false)
    private Integer currentHp;

    /**
     * 最大HP（根据等级和IV计算）
     */
    @Column(nullable = false)
    private Integer maxHp;

    /**
     * 攻击力（计算后的最终值）
     */
    @Column(nullable = false)
    private Integer attack;

    /**
     * 防御力
     */
    @Column(nullable = false)
    private Integer defense;

    /**
     * 魔法攻击力
     */
    @Column(nullable = false)
    private Integer magicAttack;

    /**
     * 魔法防御力
     */
    @Column(nullable = false)
    private Integer magicDefense;

    /**
     * 速度
     */
    @Column(nullable = false)
    private Integer speed;

    /**
     * 当前状态
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PetStatus status = PetStatus.NORMAL;

    /**
     * 亲密度 (0-255)
     */
    @Column(nullable = false)
    private Integer friendship = 50;

    /**
     * 获得时间
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * 判断是否已昏厥
     */
    public boolean isFainted() {
        return currentHp <= 0;
    }

    /**
     * 恢复HP
     */
    public void heal(int amount) {
        currentHp = Math.min(currentHp + amount, maxHp);
    }

    /**
     * 受到伤害
     */
    public void takeDamage(int damage) {
        currentHp = Math.max(currentHp - damage, 0);
    }

    /**
     * 完全恢复
     */
    public void fullRestore() {
        currentHp = maxHp;
        status = PetStatus.NORMAL;
    }
}
