package com.seele.game.entity;

import com.seele.game.enums.PetRarity;
import com.seele.game.enums.PetType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 宠物模板 - 存储宠物的基础数据（所有玩家共享）
 */
@Entity
@Table(name = "pet_template")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PetTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 宠物名称
     */
    @Column(nullable = false, length = 50)
    private String name;

    /**
     * 属性类型
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PetType type;

    /**
     * 稀有度
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PetRarity rarity;

    /**
     * 基础属性（1级时的属性值）
     */
    @Column(nullable = false)
    private Integer baseHp;

    @Column(nullable = false)
    private Integer baseAttack;

    @Column(nullable = false)
    private Integer baseDefense;

    @Column(nullable = false)
    private Integer baseMagicAttack;

    @Column(nullable = false)
    private Integer baseMagicDefense;

    @Column(nullable = false)
    private Integer baseSpeed;

    /**
     * 成长率（每升1级增加的属性值）
     */
    @Column(nullable = false)
    private Double hpGrowth;

    @Column(nullable = false)
    private Double attackGrowth;

    @Column(nullable = false)
    private Double defenseGrowth;

    @Column(nullable = false)
    private Double magicAttackGrowth;

    @Column(nullable = false)
    private Double magicDefenseGrowth;

    @Column(nullable = false)
    private Double speedGrowth;

    /**
     * 宠物描述
     */
    @Column(length = 500)
    private String description;

    /**
     * 图片URL
     */
    @Column(length = 255)
    private String imageUrl;

    /**
     * 是否为初始宠物（玩家注册时可选）
     */
    @Column(nullable = false)
    private Boolean isStarter = false;

    /**
     * 创建时间
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
}
