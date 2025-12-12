package com.seele.game.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.seele.game.entity.PetLevelSkill;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 宠物升级技能关联Mapper
 */
@Mapper
public interface PetLevelSkillMapper extends BaseMapper<PetLevelSkill> {

    /**
     * 查找某个宠物模板在指定等级可学习的技能
     */
    default List<PetLevelSkill> findByPetTemplateIdAndLearnLevel(Long petTemplateId, Integer learnLevel) {
        return selectList(lambdaQuery()
                .eq(PetLevelSkill::getPetTemplateId, petTemplateId)
                .eq(PetLevelSkill::getLearnLevel, learnLevel));
    }

    /**
     * 查找某个宠物模板的所有可学习技能
     */
    default List<PetLevelSkill> findByPetTemplateId(Long petTemplateId) {
        return selectList(lambdaQuery().eq(PetLevelSkill::getPetTemplateId, petTemplateId));
    }

    /**
     * 查找某个宠物模板在指定等级及以下可学习的所有技能
     */
    default List<PetLevelSkill> findByPetTemplateIdAndLearnLevelLessThanEqual(Long petTemplateId, Integer level) {
        return selectList(lambdaQuery()
                .eq(PetLevelSkill::getPetTemplateId, petTemplateId)
                .le(PetLevelSkill::getLearnLevel, level));
    }
}
