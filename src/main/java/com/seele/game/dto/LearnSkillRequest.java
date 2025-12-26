package com.seele.game.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 学习技能请求
 */
@Data
public class LearnSkillRequest {

    @NotNull(message = "宠物ID不能为空")
    private Long playerPetId;

    @NotNull(message = "技能ID不能为空")
    private Long skillId;

    /**
     * 如果技能栏已满，替换的位置(0-3)，null表示不装备
     */
    private Integer replacePosition;
}
