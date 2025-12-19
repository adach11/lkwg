package com.seele.game.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 战斗攻击请求
 */
@Data
public class BattleAttackRequest {
    @NotNull(message = "技能ID不能为空")
    private Long skillId;
}
