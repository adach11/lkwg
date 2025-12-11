package com.seele.game.repository;

import com.seele.game.entity.PetTemplate;
import com.seele.game.enums.PetRarity;
import com.seele.game.enums.PetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 宠物模板数据访问层
 */
@Repository
public interface PetTemplateRepository extends JpaRepository<PetTemplate, Long> {

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
}
