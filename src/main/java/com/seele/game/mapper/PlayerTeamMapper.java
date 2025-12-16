package com.seele.game.mapper;

import com.seele.game.entity.PlayerTeam;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 玩家队伍Mapper
 */
@Mapper
public interface PlayerTeamMapper {

    /**
     * 根据ID查找
     */
    PlayerTeam selectById(Long id);

    /**
     * 插入
     */
    int insert(PlayerTeam entity);

    /**
     * 根据ID更新
     */
    int updateById(PlayerTeam entity);

    /**
     * 统计总数
     */
    long selectCount();

    /**
     * 查找玩家的队伍（按位置排序）
     */
    List<PlayerTeam> findByPlayerIdOrderByPosition(Long playerId);

    /**
     * 查找玩家当前激活的队伍
     */
    List<PlayerTeam> findByPlayerIdAndIsActiveTrue(Long playerId);

    /**
     * 查找玩家指定位置的队伍成员
     */
    PlayerTeam findByPlayerIdAndPosition(Long playerId, Integer position);

    /**
     * 删除玩家的所有队伍
     */
    void deleteByPlayerId(Long playerId);
}
