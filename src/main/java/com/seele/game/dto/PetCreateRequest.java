package com.seele.game.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 选择初始宠物请求
 */
@Data
public class PetCreateRequest {

    @NotNull(message = "玩家ID不能为空")
    private Long playerId;

    @NotNull(message = "宠物模板ID不能为空")
    private Long petTemplateId;

    private String nickname;
}
