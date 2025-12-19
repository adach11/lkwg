package com.seele.game.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 战斗结束响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BattleEndResponse {
    private String battleId;
    private String winner;
    private boolean playerWon;
    private int expGained;
    private int newLevel;
    private boolean leveledUp;
    private String message;
}
