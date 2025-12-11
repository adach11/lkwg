package com.seele.game.repository;

import com.seele.game.entity.PlayerTeam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 玩家队伍数据访问层
 */
@Repository
public interface PlayerTeamRepository extends JpaRepository<PlayerTeam, Long> {

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
    Optional<PlayerTeam> findByPlayerIdAndPosition(Long playerId, Integer position);

    /**
     * 删除玩家的所有队伍
     */
    void deleteByPlayerId(Long playerId);
}
