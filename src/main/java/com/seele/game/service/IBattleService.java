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

    /**
     * 创建PVP战斗
     * @param player1Id 玩家1 ID
     * @param player1PetId 玩家1宠物ID
     * @param player2Id 玩家2 ID
     * @param player2PetId 玩家2宠物ID
     * @return 战斗状态
     */
    BattleState createPvpBattle(Long player1Id, Long player1PetId, Long player2Id, Long player2PetId);

    /**
     * 提交技能选择（PVP专用）
     * @param battleId 战斗ID
     * @param playerId 玩家ID
     * @param skillId 技能ID
     * @return 是否双方都已选择（true表示可以执行回合）
     */
    boolean submitSkillChoice(String battleId, Long playerId, Long skillId);

    /**
     * 执行PVP回合（当双方都选择好技能后自动调用）
     * @param battleId 战斗ID
     * @return 回合结果
     */
    BattleRoundResult executePvpRound(String battleId);

    /**
     * 创建队伍PVE战斗
     * @param playerId 玩家ID
     * @param petIds 玩家宠物ID列表（最多6个）
     * @param difficulty 难度
     * @return 战斗状态
     */
    BattleState createTeamPveBattle(Long playerId, Long[] petIds, AIDifficulty difficulty);

    /**
     * 创建队伍PVP战斗
     * @param player1Id 玩家1 ID
     * @param player1PetIds 玩家1宠物ID列表（最多6个）
     * @param player2Id 玩家2 ID
     * @param player2PetIds 玩家2宠物ID列表（最多6个）
     * @return 战斗状态
     */
    BattleState createTeamPvpBattle(Long player1Id, Long[] player1PetIds, Long player2Id, Long[] player2PetIds);

    /**
     * 提交切换宠物选择
     * @param battleId 战斗ID
     * @param playerId 玩家ID
     * @param switchToIndex 要切换到的宠物索引（0-5）
     * @return 是否双方都已选择（PVP模式下）
     */
    boolean submitSwitchPet(String battleId, Long playerId, int switchToIndex);
}
