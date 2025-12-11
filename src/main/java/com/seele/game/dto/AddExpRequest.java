package com.seele.game.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 增加经验值请求
 */
@Data
public class AddExpRequest {

    @NotNull(message = "宠物ID不能为空")
    private Long playerPetId;

    @Min(value = 1, message = "经验值必须大于0")
    private Integer exp;
}
