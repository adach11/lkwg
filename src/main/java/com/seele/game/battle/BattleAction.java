package com.seele.game.battle;

import com.seele.game.enums.BattleActionType;
import lombok.Data;

/**
 * 战斗动作（技能使用或切换宠物）
 */
@Data
public class BattleAction {
    /**
     * 动作类型
     */
    private BattleActionType actionType;

    /**
     * 技能ID（当actionType为USE_SKILL时有效）
     */
    private Long skillId;

    /**
     * 切换到的宠物索引（当actionType为SWITCH_PET时有效，0-5）
     */
    private Integer switchToIndex;

    /**
     * 动作优先度
     * - 切换宠物固定为8
     * - 使用技能时根据技能priority和宠物速度计算
     */
    private int priority;

    /**
     * 创建使用技能动作
     */
    public static BattleAction useSkill(Long skillId) {
        BattleAction action = new BattleAction();
        action.setActionType(BattleActionType.USE_SKILL);
        action.setSkillId(skillId);
        action.setPriority(0);  // 技能优先度默认0，后续根据技能priority和速度调整
        return action;
    }

    /**
     * 创建切换宠物动作
     */
    public static BattleAction switchPet(int switchToIndex) {
        BattleAction action = new BattleAction();
        action.setActionType(BattleActionType.SWITCH_PET);
        action.setSwitchToIndex(switchToIndex);
        action.setPriority(8);  // 切换宠物固定优先度为8
        return action;
    }

    /**
     * 是否为切换宠物动作
     */
    public boolean isSwitchAction() {
        return actionType == BattleActionType.SWITCH_PET;
    }

    /**
     * 是否为技能动作
     */
    public boolean isSkillAction() {
        return actionType == BattleActionType.USE_SKILL;
    }
}
