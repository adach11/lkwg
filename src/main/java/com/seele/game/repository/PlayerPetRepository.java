package com.seele.game.repository;

import com.seele.game.entity.PlayerPet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 玩家宠物数据访问层
 */
@Repository
public interface PlayerPetRepository extends JpaRepository<PlayerPet, Long> {

    /**
     * 查找某个玩家的所有宠物
     */
    List<PlayerPet> findByPlayerId(Long playerId);

    /**
     * 查找某个玩家的指定宠物模板实例
     */
    List<PlayerPet> findByPlayerIdAndPetTemplateId(Long playerId, Long petTemplateId);

    /**
     * 查找玩家宠物数量
     */
    long countByPlayerId(Long playerId);

    /**
     * 查找玩家所有未昏厥的宠物
     */
    @Query("SELECT p FROM PlayerPet p WHERE p.playerId = :playerId AND p.currentHp > 0")
    List<PlayerPet> findActivePetsByPlayerId(@Param("playerId") Long playerId);
}
