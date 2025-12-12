package com.seele.game.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.seele.game.entity.PlayerPet;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 玩家宠物Mapper
 */
@Mapper
public interface PlayerPetMapper extends BaseMapper<PlayerPet> {

    /**
     * 查找某个玩家的所有宠物
     */
    default List<PlayerPet> findByPlayerId(Long playerId) {
        return selectList(lambdaQuery().eq(PlayerPet::getPlayerId, playerId));
    }

    /**
     * 查找某个玩家的指定宠物模板实例
     */
    default List<PlayerPet> findByPlayerIdAndPetTemplateId(Long playerId, Long petTemplateId) {
        return selectList(lambdaQuery()
                .eq(PlayerPet::getPlayerId, playerId)
                .eq(PlayerPet::getPetTemplateId, petTemplateId));
    }

    /**
     * 查找玩家宠物数量
     */
    default long countByPlayerId(Long playerId) {
        return selectCount(lambdaQuery().eq(PlayerPet::getPlayerId, playerId));
    }

    /**
     * 查找玩家所有未昏厥的宠物
     */
    default List<PlayerPet> findActivePetsByPlayerId(Long playerId) {
        return selectList(lambdaQuery()
                .eq(PlayerPet::getPlayerId, playerId)
                .gt(PlayerPet::getCurrentHp, 0));
    }
}
