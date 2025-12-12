package com.seele.game.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.seele.game.entity.PetTemplate;
import com.seele.game.enums.PetRarity;
import com.seele.game.enums.PetType;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 宠物模板Mapper
 */
@Mapper
public interface PetTemplateMapper extends BaseMapper<PetTemplate> {

    /**
     * 查找所有初始宠物
     */
    default List<PetTemplate> findByIsStarterTrue() {
        return selectList(lambdaQuery().eq(PetTemplate::getIsStarter, true));
    }

    /**
     * 根据属性类型查找宠物
     */
    default List<PetTemplate> findByType(PetType type) {
        return selectList(lambdaQuery().eq(PetTemplate::getType, type));
    }

    /**
     * 根据稀有度查找宠物
     */
    default List<PetTemplate> findByRarity(PetRarity rarity) {
        return selectList(lambdaQuery().eq(PetTemplate::getRarity, rarity));
    }

    /**
     * 根据名称查找宠物
     */
    default PetTemplate findByName(String name) {
        return selectOne(lambdaQuery().eq(PetTemplate::getName, name));
    }
}
