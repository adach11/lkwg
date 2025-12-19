package com.seele.game.entity;

import com.seele.game.enums.PetStatus;
import com.seele.game.enums.PetType;
import com.seele.game.enums.SkillType;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 技能
 */
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Skill {

    private Long id;

    /**
     * 技能名称
     */
    private String name;

    /**
     * 技能类型：物理/魔法/变化
     */
    private SkillType skillType;

    /**
     * 技能属性
     */
    private PetType petType;

    /**
     * 威力 (0-150，0表示变化技能)
     */
    private Integer power;

    /**
     * 命中率 (30-100)
     */
    private Integer accuracy;

    /**
     * 最大使用次数（PP）
     */
    private Integer maxPp;

    /**
     * 先制度 (-7 到 +7)
     * 正数表示优先级高，负数表示后手
     */
    private Integer priority;

    /**
     * 附加状态效果
     */
    private PetStatus statusEffect;

    /**
     * 状态效果触发概率 (0-100)
     */
    private Integer effectChance;

    /**
     * 技能描述
     */
    private String description;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
