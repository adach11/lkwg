package com.seele.game.service;

import com.seele.game.battle.BattleAction;
import com.seele.game.battle.BattlePetState;
import com.seele.game.entity.Skill;

/**
 * 战斗计算服务接口
 */
public interface IBattleCalculationService {

    /**
     * 计算伤害
     * @param attacker 攻击方
     * @param defender 防守方
     * @param skill 使用的技能
     * @param isCritical 是否暴击
     * @return 伤害值
     */
    int calculateDamage(BattlePetState attacker, BattlePetState defender, Skill skill, boolean isCritical);

    /**
     * 检查是否命中
     * @param skill 技能
     * @return 是否命中
     */
    boolean checkHit(Skill skill);

    /**
     * 检查是否暴击
     * @return 是否暴击
     */
    boolean checkCritical();

    /**
     * 获取有效速度（考虑属性变化和状态）
     * @param pet 宠物状态
     * @return 有效速度
     */
    int getEffectiveSpeed(BattlePetState pet);

    /**
     * 计算属性克制倍率
     * @param attacker 攻击方
     * @param defender 防守方
     * @param skill 技能
     * @return 克制倍率
     */
    double getTypeEffectiveness(BattlePetState attacker, BattlePetState defender, Skill skill);

    /**
     * 处理回合结束的状态伤害（烧伤、中毒）
     * @param pet 宠物状态
     * @return 伤害消息（如果有）
     */
    String processEndOfTurnStatus(BattlePetState pet);

    /**
     * 检查是否可以行动（麻痹、冰冻、睡眠）
     * @param pet 宠物状态
     * @return 行动结果消息（null表示可以行动）
     */
    String checkCanAct(BattlePetState pet);

    /**
     * 判断谁先行动
     * @param pet1 宠物1状态
     * @param action1 宠物1的动作
     * @param pet2 宠物2状态
     * @param action2 宠物2的动作
     * @return true表示宠物1先行动，false表示宠物2先行动
     */
    boolean determineFirstMove(BattlePetState pet1, BattleAction action1, BattlePetState pet2, BattleAction action2);
}
