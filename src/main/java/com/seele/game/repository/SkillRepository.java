package com.seele.game.repository;

import com.seele.game.entity.Skill;
import com.seele.game.enums.PetType;
import com.seele.game.enums.SkillType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 技能数据访问层
 */
@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {

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
