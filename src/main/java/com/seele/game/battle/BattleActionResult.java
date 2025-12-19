package com.seele.game.battle;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 单次战斗行动结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BattleActionResult {
    // 行动者名称
    private String actorName;

    // 技能名称
    private String skillName;

    // 是否命中
    private boolean hit;

    // 造成的伤害
    private int damage;

    // 是否暴击
    private boolean critical;

    // 属性克制倍率
    private double typeEffectiveness;

    // 是否触发了状态异常
    private String statusEffect;

    // 行动描述
    private String message;

    // 目标是否阵亡
    private boolean targetFainted;
}
