package com.seele.game.service;

import com.seele.game.entity.PlayerTeam;

import java.util.List;

/**
 * 玩家队伍服务接口
 */
public interface IPlayerTeamService {

    /**
     * 获取玩家的队伍（按位置排序）
     * @param playerId 玩家ID
     * @return 队伍列表
     */
    List<PlayerTeam> getPlayerTeam(Long playerId);

    /**
     * 获取玩家当前激活的队伍
     * @param playerId 玩家ID
     * @return 激活的队伍列表
     */
    List<PlayerTeam> getActiveTeam(Long playerId);

    /**
     * 设置队伍中的宠物
     * @param playerId 玩家ID
     * @param position 位置（1-6）
     * @param petId 宠物ID（null表示移除）
     * @return 更新后的队伍成员
     */
    PlayerTeam setTeamMember(Long playerId, Integer position, Long petId);

    /**
     * 移除队伍中指定位置的宠物
     * @param playerId 玩家ID
     * @param position 位置（1-6）
     */
    void removeTeamMember(Long playerId, Integer position);

    /**
     * 获取玩家队伍中的宠物ID列表（过滤空位）
     * @param playerId 玩家ID
     * @return 宠物ID数组
     */
    Long[] getTeamPetIds(Long playerId);

    /**
     * 批量设置队伍
     * @param playerId 玩家ID
     * @param petIds 宠物ID列表（最多6个）
     */
    void setTeam(Long playerId, Long[] petIds);

    /**
     * 清空玩家队伍
     * @param playerId 玩家ID
     */
    void clearTeam(Long playerId);
}
