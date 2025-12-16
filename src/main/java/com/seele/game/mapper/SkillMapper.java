package com.seele.game.mapper;

import com.seele.game.entity.Skill;
import com.seele.game.enums.PetType;
import com.seele.game.enums.SkillType;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 技能Mapper
 */
@Mapper
public interface SkillMapper {

    /**
     * 根据ID查找
     */
    Skill selectById(Long id);

    /**
     * 插入
     */
    int insert(Skill entity);

    /**
     * 根据ID更新
     */
    int updateById(Skill entity);

    /**
     * 统计总数
     */
    long selectCount();

    /**
     * 根据技能类型查找
     */
    List<Skill> findBySkillType(SkillType skillType);

    /**
     * 根据属性类型查找
     */
    List<Skill> findByPetType(PetType petType);

    /**
     * 根据名称查找技能
     */
    Skill findByName(String name);
}
