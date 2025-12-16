package com.seele.game.mapper;

import com.seele.game.entity.PlayerPet;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 玩家宠物Mapper
 */
@Mapper
public interface PlayerPetMapper {

    /**
     * 根据ID查找
     */
    PlayerPet selectById(Long id);

    /**
     * 插入
     */
    int insert(PlayerPet entity);

    /**
     * 根据ID更新
     */
    int updateById(PlayerPet entity);

    /**
     * 统计总数
     */
    long selectCount();

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
    List<PlayerPet> findActivePetsByPlayerId(Long playerId);
}
