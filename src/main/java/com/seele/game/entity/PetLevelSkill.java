package com.seele.game.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 宠物升级学习技能关联表
 * 定义某个宠物在特定等级可以学习哪些技能
 */
@Entity
@Table(name = "pet_level_skill",
       uniqueConstraints = @UniqueConstraint(columnNames = {"pet_template_id", "skill_id", "learn_level"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PetLevelSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 宠物模板ID
     */
    @Column(nullable = false)
    private Long petTemplateId;

    /**
     * 技能ID
     */
    @Column(nullable = false)
    private Long skillId;

    /**
     * 在几级学会该技能
     */
    @Column(nullable = false)
    private Integer learnLevel;

    /**
     * 创建时间
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
