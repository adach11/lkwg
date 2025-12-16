package com.seele.game.service.impl;

import com.seele.game.entity.PetLevelSkill;
import com.seele.game.entity.PlayerPet;
import com.seele.game.entity.PlayerPetSkill;
import com.seele.game.entity.Skill;
import com.seele.game.mapper.PetLevelSkillMapper;
import com.seele.game.mapper.PlayerPetSkillMapper;
import com.seele.game.mapper.SkillMapper;
import com.seele.game.service.ISkillLearnService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 技能学习服务实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SkillLearnServiceImpl implements ISkillLearnService {

    private final PetLevelSkillMapper petLevelSkillMapper;
    private final PlayerPetSkillMapper playerPetSkillMapper;
    private final SkillMapper skillMapper;

    @Override
    @Transactional
    public void checkAndLearnSkillsOnLevelUp(PlayerPet playerPet, int newLevel) {
        List<PetLevelSkill> learnableSkills = petLevelSkillMapper
                .findByPetTemplateIdAndLearnLevel(playerPet.getPetTemplateId(), newLevel);

        for (PetLevelSkill levelSkill : learnableSkills) {
            // 检查是否已学会
            PlayerPetSkill existing = playerPetSkillMapper
                    .findByPlayerPetIdAndSkillId(playerPet.getId(), levelSkill.getSkillId());

            if (existing == null) {
                learnSkill(playerPet.getId(), levelSkill.getSkillId(), null);
                log.info("宠物{}在{}级学会了技能{}", playerPet.getId(), newLevel, levelSkill.getSkillId());
            }
        }
    }

    @Override
    @Transactional
    public PlayerPetSkill learnSkill(Long playerPetId, Long skillId, Integer replacePosition) {
        // 检查是否已学会
        PlayerPetSkill existing = playerPetSkillMapper
                .findByPlayerPetIdAndSkillId(playerPetId, skillId);

        if (existing != null) {
            log.warn("宠物{}已经学会技能{}", playerPetId, skillId);
            return existing;
        }

        Skill skill = skillMapper.selectById(skillId);
        if (skill == null) {
            throw new IllegalArgumentException("技能不存在");
        }

        PlayerPetSkill playerPetSkill = new PlayerPetSkill();
        playerPetSkill.setPlayerPetId(playerPetId);
        playerPetSkill.setSkillId(skillId);
        playerPetSkill.setMaxPp(skill.getMaxPp());
        playerPetSkill.setCurrentPp(skill.getMaxPp());

        // 检查已装备技能数量
        long equippedCount = playerPetSkillMapper.countEquippedSkills(playerPetId);

        if (equippedCount < 4) {
            // 技能栏未满，自动装备
            playerPetSkill.setIsEquipped(true);
            playerPetSkill.setPosition((int) equippedCount);
        } else if (replacePosition != null && replacePosition >= 0 && replacePosition < 4) {
            // 替换指定位置的技能
            replaceSkill(playerPetId, replacePosition, playerPetSkill);
        } else {
            // 技能栏已满且不替换，只学会不装备
            playerPetSkill.setIsEquipped(false);
            playerPetSkill.setPosition(null);
        }

        playerPetSkillMapper.insert(playerPetSkill);
        return playerPetSkill;
    }

    /**
     * 替换技能位置的技能
     */
    private void replaceSkill(Long playerPetId, int position, PlayerPetSkill newSkill) {
        PlayerPetSkill oldSkill = playerPetSkillMapper
                .findByPlayerPetIdAndPosition(playerPetId, position);

        if (oldSkill != null) {
            // 旧技能取消装备
            oldSkill.setIsEquipped(false);
            oldSkill.setPosition(null);
            playerPetSkillMapper.updateById(oldSkill);
        }

        // 新技能装备到该位置
        newSkill.setIsEquipped(true);
        newSkill.setPosition(position);
    }

    @Override
    @Transactional
    public void equipSkill(Long playerPetId, Long skillId, int position) {
        if (position < 0 || position > 3) {
            throw new IllegalArgumentException("技能位置必须在0-3之间");
        }

        PlayerPetSkill skill = playerPetSkillMapper
                .findByPlayerPetIdAndSkillId(playerPetId, skillId);
        if (skill == null) {
            throw new IllegalArgumentException("宠物未学会该技能");
        }

        // 如果该位置已有技能，先卸下
        PlayerPetSkill oldSkill = playerPetSkillMapper
                .findByPlayerPetIdAndPosition(playerPetId, position);

        if (oldSkill != null && !oldSkill.getId().equals(skill.getId())) {
            oldSkill.setIsEquipped(false);
            oldSkill.setPosition(null);
            playerPetSkillMapper.updateById(oldSkill);
        }

        // 装备新技能
        skill.setIsEquipped(true);
        skill.setPosition(position);
        playerPetSkillMapper.updateById(skill);

        log.info("宠物{}在位置{}装备了技能{}", playerPetId, position, skillId);
    }

    @Override
    @Transactional
    public void unequipSkill(Long playerPetId, Long skillId) {
        PlayerPetSkill skill = playerPetSkillMapper
                .findByPlayerPetIdAndSkillId(playerPetId, skillId);
        if (skill == null) {
            throw new IllegalArgumentException("宠物未学会该技能");
        }

        skill.setIsEquipped(false);
        skill.setPosition(null);
        playerPetSkillMapper.updateById(skill);

        log.info("宠物{}卸下了技能{}", playerPetId, skillId);
    }

    @Override
    public List<PlayerPetSkill> getLearnedSkills(Long playerPetId) {
        return playerPetSkillMapper.findByPlayerPetId(playerPetId);
    }

    @Override
    public List<PlayerPetSkill> getEquippedSkills(Long playerPetId) {
        return playerPetSkillMapper.findByPlayerPetIdAndIsEquippedTrue(playerPetId);
    }

    @Override
    @Transactional
    public void restoreAllPp(Long playerPetId) {
        List<PlayerPetSkill> skills = playerPetSkillMapper.findByPlayerPetId(playerPetId);
        skills.forEach(PlayerPetSkill::restorePp);
        skills.forEach(skill -> playerPetSkillMapper.updateById(skill));
        log.info("恢复宠物{}所有技能的PP", playerPetId);
    }
}
