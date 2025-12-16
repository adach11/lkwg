package com.seele.game.mapper;

import com.seele.game.entity.PetLevelSkill;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 宠物升级技能关联Mapper
 */
@Mapper
public interface PetLevelSkillMapper {

    /**
     * 根据ID查找
     */
    PetLevelSkill selectById(Long id);

    /**
     * 插入
     */
    int insert(PetLevelSkill entity);

    /**
     * 根据ID更新
     */
    int updateById(PetLevelSkill entity);

    /**
     * 统计总数
     */
    long selectCount();

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
