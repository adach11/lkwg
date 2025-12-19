package com.seele.game.mapper;

import com.seele.game.entity.PetTemplate;
import com.seele.game.enums.PetRarity;
import com.seele.game.enums.PetType;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 宠物模板Mapper
 */
@Mapper
public interface PetTemplateMapper {

    /**
     * 根据ID查找
     */
    PetTemplate selectById(Long id);

    /**
     * 插入
     */
    int insert(PetTemplate entity);

    /**
     * 根据ID更新
     */
    int updateById(PetTemplate entity);

    /**
     * 统计总数
     */
    long selectCount();

    /**
     * 查找所有初始宠物
     */
    List<PetTemplate> findByIsStarterTrue();

    /**
     * 根据属性类型查找宠物
     */
    List<PetTemplate> findByType(PetType type);

    /**
     * 根据稀有度查找宠物
     */
    List<PetTemplate> findByRarity(PetRarity rarity);

    /**
     * 根据名称查找宠物
     */
    PetTemplate findByName(String name);

    /**
     * 查找所有宠物模板
     */
    List<PetTemplate> findAll();
}
