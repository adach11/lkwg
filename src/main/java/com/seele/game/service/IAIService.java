package com.seele.game.service;

import com.seele.game.battle.BattlePetState;
import com.seele.game.battle.BattleState;
import com.seele.game.entity.PlayerPet;
import com.seele.game.entity.Skill;
import com.seele.game.enums.AIDifficulty;

import java.util.List;

/**
 * AI决策服务接口
 */
public interface IAIService {

    /**
     * 根据玩家宠物生成对应等级的AI宠物
     * @param playerPet 玩家宠物
     * @param difficulty 难度
     * @return AI宠物
     */
    PlayerPet generateAIPet(PlayerPet playerPet, AIDifficulty difficulty);

    /**
     * AI选择技能
     * @param battleState 战斗状态
     * @return 选择的技能
     */
    Skill selectSkill(BattleState battleState);

    /**
     * 评估技能的价值
     * @param skill 技能
     * @param attacker 攻击方
     * @param defender 防守方
     * @param difficulty 难度
     * @return 技能价值评分
     */
    double evaluateSkill(Skill skill, BattlePetState attacker, BattlePetState defender, AIDifficulty difficulty);
}
