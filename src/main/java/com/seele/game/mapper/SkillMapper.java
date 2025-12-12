package com.seele.game.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.seele.game.entity.Skill;
import com.seele.game.enums.PetType;
import com.seele.game.enums.SkillType;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 技能Mapper
 */
@Mapper
public interface SkillMapper extends BaseMapper<Skill> {

    /**
     * 根据技能类型查找
     */
    default List<Skill> findBySkillType(SkillType skillType) {
        return selectList(lambdaQuery().eq(Skill::getSkillType, skillType));
    }

    /**
     * 根据属性类型查找
     */
    default List<Skill> findByPetType(PetType petType) {
        return selectList(lambdaQuery().eq(Skill::getPetType, petType));
    }

    /**
     * 根据名称查找技能
     */
    default Skill findByName(String name) {
        return selectOne(lambdaQuery().eq(Skill::getName, name));
    }
}
