package com.seele.game.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.seele.game.entity.PlayerTeam;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 玩家队伍Mapper
 */
@Mapper
public interface PlayerTeamMapper extends BaseMapper<PlayerTeam> {

    /**
     * 查找玩家的队伍（按位置排序）
     */
    default List<PlayerTeam> findByPlayerIdOrderByPosition(Long playerId) {
        return selectList(lambdaQuery()
                .eq(PlayerTeam::getPlayerId, playerId)
                .orderByAsc(PlayerTeam::getPosition));
    }

    /**
     * 查找玩家当前激活的队伍
     */
    default List<PlayerTeam> findByPlayerIdAndIsActiveTrue(Long playerId) {
        return selectList(lambdaQuery()
                .eq(PlayerTeam::getPlayerId, playerId)
                .eq(PlayerTeam::getIsActive, true));
    }

    /**
     * 查找玩家指定位置的队伍成员
     */
    default PlayerTeam findByPlayerIdAndPosition(Long playerId, Integer position) {
        return selectOne(lambdaQuery()
                .eq(PlayerTeam::getPlayerId, playerId)
                .eq(PlayerTeam::getPosition, position));
    }

    /**
     * 删除玩家的所有队伍
     */
    default void deleteByPlayerId(Long playerId) {
        delete(lambdaQuery().eq(PlayerTeam::getPlayerId, playerId));
    }
}
