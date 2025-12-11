package com.seele.game.entity;

import com.seele.game.enums.PetStatus;
import com.seele.game.enums.PetType;
import com.seele.game.enums.SkillType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 技能
 */
@Entity
@Table(name = "skill")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Skill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 技能名称
     */
    @Column(nullable = false, length = 50)
    private String name;

    /**
     * 技能类型：物理/魔法/变化
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SkillType skillType;

    /**
     * 技能属性
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PetType petType;

    /**
     * 威力 (0-150，0表示变化技能)
     */
    @Column(nullable = false)
    private Integer power;

    /**
     * 命中率 (30-100)
     */
    @Column(nullable = false)
    private Integer accuracy;

    /**
     * 最大使用次数（PP）
     */
    @Column(nullable = false)
    private Integer maxPp;

    /**
     * 先制度 (-7 到 +7)
     * 正数表示优先级高，负数表示后手
     */
    @Column(nullable = false)
    private Integer priority = 0;

    /**
     * 附加状态效果
     */
    @Enumerated(EnumType.STRING)
    private PetStatus statusEffect;

    /**
     * 状态效果触发概率 (0-100)
     */
    private Integer effectChance;

    /**
     * 技能描述
     */
    @Column(length = 500)
    private String description;

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
