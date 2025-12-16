package com.seele.game.service;

import com.seele.game.entity.PlayerPet;
import com.seele.game.entity.PlayerPetSkill;

import java.util.List;

/**
 * 技能学习服务接口
 */
public interface ISkillLearnService {

    /**
     * 检查宠物升级时是否学会新技能
     * @param playerPet 玩家宠物
     * @param newLevel 新等级
     */
    void checkAndLearnSkillsOnLevelUp(PlayerPet playerPet, int newLevel);

    /**
     * 学习技能
     * @param playerPetId 玩家宠物ID
     * @param skillId 技能ID
     * @param replacePosition 如果技能栏已满，替换的位置(0-3)，null表示不装备
     * @return 学习的技能
     */
    PlayerPetSkill learnSkill(Long playerPetId, Long skillId, Integer replacePosition);

    /**
     * 装备技能到指定位置
     * @param playerPetId 玩家宠物ID
     * @param skillId 技能ID
     * @param position 位置 (0-3)
     */
    void equipSkill(Long playerPetId, Long skillId, int position);

    /**
     * 卸下技能
     */
    void unequipSkill(Long playerPetId, Long skillId);

    /**
     * 获取宠物已学会的所有技能
     */
    List<PlayerPetSkill> getLearnedSkills(Long playerPetId);

    /**
     * 获取宠物已装备的技能
     */
    List<PlayerPetSkill> getEquippedSkills(Long playerPetId);

    /**
     * 恢复所有技能的PP
     */
    void restoreAllPp(Long playerPetId);
}
