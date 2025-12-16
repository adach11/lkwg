package com.seele.game.entity;

import com.seele.game.enums.PetRarity;
import com.seele.game.enums.PetType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 宠物模板 - 存储宠物的基础数据（所有玩家共享）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PetTemplate {

    private Long id;

    /**
     * 宠物名称
     */
    private String name;

    /**
     * 属性类型
     */
    private PetType type;

    /**
     * 稀有度
     */
    private PetRarity rarity;

    /**
     * 基础属性（1级时的属性值）
     */
    private Integer baseHp;

    private Integer baseAttack;

    private Integer baseDefense;

    private Integer baseMagicAttack;

    private Integer baseMagicDefense;

    private Integer baseSpeed;

    /**
     * 成长率（每升1级增加的属性值）
     */
    private Double hpGrowth;

    private Double attackGrowth;

    private Double defenseGrowth;

    private Double magicAttackGrowth;

    private Double magicDefenseGrowth;

    private Double speedGrowth;

    /**
     * 宠物描述
     */
    private String description;

    /**
     * 图片URL
     */
    private String imageUrl;

    /**
     * 是否为初始宠物（玩家注册时可选）
     */
    private Boolean isStarter;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
