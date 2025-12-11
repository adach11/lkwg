package com.seele.game.repository;

import com.seele.game.entity.PetLevelSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 宠物升级技能关联数据访问层
 */
@Repository
public interface PetLevelSkillRepository extends JpaRepository<PetLevelSkill, Long> {

    /**
     * 查找某个宠物模板在指定等级可学习的技能
     */
    List<PetLevelSkill> findByPetTemplateIdAndLearnLevel(Long petTemplateId, Integer learnLevel);

    /**
     * 查找某个宠物模板的所有可学习技能
     */
    List<PetLevelSkill> findByPetTemplateId(Long petTemplateId);

    /**
     * 查找某个宠物模板在指定等级及以下可学习的所有技能
     */
    List<PetLevelSkill> findByPetTemplateIdAndLearnLevelLessThanEqual(Long petTemplateId, Integer level);
}
