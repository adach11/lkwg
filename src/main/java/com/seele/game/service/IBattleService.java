package com.seele.game.service;

import com.seele.game.battle.BattleRoundResult;
import com.seele.game.battle.BattleState;
import com.seele.game.dto.BattleEndResponse;
import com.seele.game.enums.AIDifficulty;

/**
 * 战斗服务接口
 */
public interface IBattleService {

    /**
     * 创建人机对战
     * @param playerId 玩家ID
     * @param petId 宠物ID
     * @param difficulty 难度
     * @return 战斗状态
     */
    BattleState createPveBattle(Long playerId, Long petId, AIDifficulty difficulty);

    /**
     * 获取战斗状态
     * @param battleId 战斗ID
     * @return 战斗状态
     */
    BattleState getBattleState(String battleId);

    /**
     * 执行战斗回合
     * @param battleId 战斗ID
     * @param playerId 玩家ID（用于验证）
     * @param playerSkillId 玩家选择的技能ID
     * @return 回合结果
     */
    BattleRoundResult executeRound(String battleId, Long playerId, Long playerSkillId);

    /**
     * 结束战斗（投降或超时）
     * @param battleId 战斗ID
     * @return 战斗结束响应
     */
    BattleEndResponse endBattle(String battleId);

    /**
     * 清理超时的战斗
     */
    void cleanupInactiveBattles();
}
