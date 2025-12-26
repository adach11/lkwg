package com.seele.game.dto;

import com.seele.game.enums.PetStatus;
import com.seele.game.enums.PetType;
import com.seele.game.enums.SkillType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建技能请求
 */
@Data
public class SkillCreateRequest {

    @NotNull(message = "技能名称不能为空")
    private String name;

    @NotNull(message = "技能类型不能为空")
    private SkillType skillType;

    @NotNull(message = "技能属性不能为空")
    private PetType petType;

    /**
     * 威力 (0-150，0表示变化技能)
     */
    @Min(value = 0, message = "威力不能小于0")
    @Max(value = 150, message = "威力不能大于150")
    private Integer power;

    /**
     * 命中率 (30-100)
     */
    @Min(value = 30, message = "命中率不能小于30")
    @Max(value = 100, message = "命中率不能大于100")
    private Integer accuracy;

    /**
     * 最大使用次数（PP）
     */
    @Min(value = 1, message = "PP不能小于1")
    private Integer maxPp;

    /**
     * 先制度 (-7 到 +7)
     */
    @Min(value = -7, message = "先制度不能小于-7")
    @Max(value = 7, message = "先制度不能大于7")
    private Integer priority;

    /**
     * 附加状态效果
     */
    private PetStatus statusEffect;

    /**
     * 状态效果触发概率 (0-100)
     */
    @Min(value = 0, message = "效果概率不能小于0")
    @Max(value = 100, message = "效果概率不能大于100")
    private Integer effectChance;

    /**
     * 技能描述
     */
    private String description;
}
