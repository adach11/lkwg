package com.seele.game.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.seele.game.entity.PlayerPetSkill;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 玩家宠物技能Mapper
 */
@Mapper
public interface PlayerPetSkillMapper extends BaseMapper<PlayerPetSkill> {

    /**
     * 查找某个宠物已学会的所有技能
     */
    default List<PlayerPetSkill> findByPlayerPetId(Long playerPetId) {
        return selectList(lambdaQuery().eq(PlayerPetSkill::getPlayerPetId, playerPetId));
    }

    /**
     * 查找某个宠物已装备的技能
     */
    default List<PlayerPetSkill> findByPlayerPetIdAndIsEquippedTrue(Long playerPetId) {
        return selectList(lambdaQuery()
                .eq(PlayerPetSkill::getPlayerPetId, playerPetId)
                .eq(PlayerPetSkill::getIsEquipped, true));
    }

    /**
     * 查找某个宠物是否已学会某个技能
     */
    default PlayerPetSkill findByPlayerPetIdAndSkillId(Long playerPetId, Long skillId) {
        return selectOne(lambdaQuery()
                .eq(PlayerPetSkill::getPlayerPetId, playerPetId)
                .eq(PlayerPetSkill::getSkillId, skillId));
    }

    /**
     * 统计某个宠物已装备的技能数量
     */
    default long countEquippedSkills(Long playerPetId) {
        return selectCount(lambdaQuery()
                .eq(PlayerPetSkill::getPlayerPetId, playerPetId)
                .eq(PlayerPetSkill::getIsEquipped, true));
    }

    /**
     * 查找宠物指定位置的技能
     */
    default PlayerPetSkill findByPlayerPetIdAndPosition(Long playerPetId, Integer position) {
        return selectOne(lambdaQuery()
                .eq(PlayerPetSkill::getPlayerPetId, playerPetId)
                .eq(PlayerPetSkill::getPosition, position));
    }
}
