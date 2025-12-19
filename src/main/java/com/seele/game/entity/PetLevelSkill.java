package com.seele.game.entity;

import lombok.*;

import java.time.LocalDateTime;

/**
 * 宠物升级学习技能关联表
 * 定义某个宠物在特定等级可以学习哪些技能
 */
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PetLevelSkill {

    private Long id;

    /**
     * 宠物模板ID
     */
    private Long petTemplateId;

    /**
     * 技能ID
     */
    private Long skillId;

    /**
     * 在几级学会该技能
     */
    private Integer learnLevel;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
