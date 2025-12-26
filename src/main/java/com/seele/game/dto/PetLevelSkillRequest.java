package com.seele.game.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 设置宠物升级学习技能请求
 */
@Data
public class PetLevelSkillRequest {

    @NotNull(message = "宠物模板ID不能为空")
    private Long petTemplateId;

    @NotNull(message = "技能ID不能为空")
    private Long skillId;

    @NotNull(message = "学习等级不能为空")
    @Min(value = 1, message = "学习等级不能小于1")
    private Integer learnLevel;
}
