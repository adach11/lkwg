package com.seele.game.dto;

import com.seele.game.enums.AIDifficulty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 开始战斗请求
 */
@Data
public class BattleStartRequest {
    @NotNull(message = "玩家ID不能为空")
    private Long playerId;

    @NotNull(message = "宠物ID不能为空")
    private Long petId;

    @NotNull(message = "难度不能为空")
    private AIDifficulty difficulty;
}
