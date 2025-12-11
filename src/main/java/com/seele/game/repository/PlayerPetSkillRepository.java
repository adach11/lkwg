package com.seele.game.repository;

import com.seele.game.entity.PlayerPetSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 玩家宠物技能数据访问层
 */
@Repository
public interface PlayerPetSkillRepository extends JpaRepository<PlayerPetSkill, Long> {

    /**
     * 查找某个宠物已学会的所有技能
     */
    List<PlayerPetSkill> findByPlayerPetId(Long playerPetId);

    /**
     * 查找某个宠物已装备的技能
     */
    List<PlayerPetSkill> findByPlayerPetIdAndIsEquippedTrue(Long playerPetId);

    /**
     * 查找某个宠物是否已学会某个技能
     */
    Optional<PlayerPetSkill> findByPlayerPetIdAndSkillId(Long playerPetId, Long skillId);

    /**
     * 统计某个宠物已装备的技能数量
     */
    @Query("SELECT COUNT(p) FROM PlayerPetSkill p WHERE p.playerPetId = :playerPetId AND p.isEquipped = true")
    long countEquippedSkills(@Param("playerPetId") Long playerPetId);

    /**
     * 查找宠物指定位置的技能
     */
    Optional<PlayerPetSkill> findByPlayerPetIdAndPosition(Long playerPetId, Integer position);
}
