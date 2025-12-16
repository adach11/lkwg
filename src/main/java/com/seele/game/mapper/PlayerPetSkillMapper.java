package com.seele.game.mapper;

import com.seele.game.entity.PlayerPetSkill;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 玩家宠物技能Mapper
 */
@Mapper
public interface PlayerPetSkillMapper {

    /**
     * 根据ID查找
     */
    PlayerPetSkill selectById(Long id);

    /**
     * 插入
     */
    int insert(PlayerPetSkill entity);

    /**
     * 根据ID更新
     */
    int updateById(PlayerPetSkill entity);

    /**
     * 统计总数
     */
    long selectCount();

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
    PlayerPetSkill findByPlayerPetIdAndSkillId(Long playerPetId, Long skillId);

    /**
     * 统计某个宠物已装备的技能数量
     */
    long countEquippedSkills(Long playerPetId);

    /**
     * 查找宠物指定位置的技能
     */
    PlayerPetSkill findByPlayerPetIdAndPosition(Long playerPetId, Integer position);
}
