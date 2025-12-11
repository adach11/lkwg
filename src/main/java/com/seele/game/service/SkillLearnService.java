package com.seele.game.service;

import com.seele.game.entity.PetLevelSkill;
import com.seele.game.entity.PlayerPet;
import com.seele.game.entity.PlayerPetSkill;
import com.seele.game.entity.Skill;
import com.seele.game.repository.PetLevelSkillRepository;
import com.seele.game.repository.PlayerPetSkillRepository;
import com.seele.game.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 技能学习服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SkillLearnService {

    private final PetLevelSkillRepository petLevelSkillRepository;
    private final PlayerPetSkillRepository playerPetSkillRepository;
    private final SkillRepository skillRepository;

    /**
     * 检查宠物升级时是否学会新技能
     * @param playerPet 玩家宠物
     * @param newLevel 新等级
     */
    @Transactional
    public void checkAndLearnSkillsOnLevelUp(PlayerPet playerPet, int newLevel) {
        List<PetLevelSkill> learnableSkills = petLevelSkillRepository
                .findByPetTemplateIdAndLearnLevel(playerPet.getPetTemplateId(), newLevel);

        for (PetLevelSkill levelSkill : learnableSkills) {
            // 检查是否已学会
            Optional<PlayerPetSkill> existing = playerPetSkillRepository
                    .findByPlayerPetIdAndSkillId(playerPet.getId(), levelSkill.getSkillId());

            if (existing.isEmpty()) {
                learnSkill(playerPet.getId(), levelSkill.getSkillId(), null);
                log.info("宠物{}在{}级学会了技能{}", playerPet.getId(), newLevel, levelSkill.getSkillId());
            }
        }
    }

    /**
     * 学习技能
     * @param playerPetId 玩家宠物ID
     * @param skillId 技能ID
     * @param replacePosition 如果技能栏已满，替换的位置(0-3)，null表示不装备
     * @return 学习的技能
     */
    @Transactional
    public PlayerPetSkill learnSkill(Long playerPetId, Long skillId, Integer replacePosition) {
        // 检查是否已学会
        Optional<PlayerPetSkill> existing = playerPetSkillRepository
                .findByPlayerPetIdAndSkillId(playerPetId, skillId);

        if (existing.isPresent()) {
            log.warn("宠物{}已经学会技能{}", playerPetId, skillId);
            return existing.get();
        }

        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new IllegalArgumentException("技能不存在"));

        PlayerPetSkill playerPetSkill = new PlayerPetSkill();
        playerPetSkill.setPlayerPetId(playerPetId);
        playerPetSkill.setSkillId(skillId);
        playerPetSkill.setMaxPp(skill.getMaxPp());
        playerPetSkill.setCurrentPp(skill.getMaxPp());

        // 检查已装备技能数量
        long equippedCount = playerPetSkillRepository.countEquippedSkills(playerPetId);

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

        return playerPetSkillRepository.save(playerPetSkill);
    }

    /**
     * 替换技能位置的技能
     */
    private void replaceSkill(Long playerPetId, int position, PlayerPetSkill newSkill) {
        Optional<PlayerPetSkill> oldSkill = playerPetSkillRepository
                .findByPlayerPetIdAndPosition(playerPetId, position);

        if (oldSkill.isPresent()) {
            // 旧技能取消装备
            PlayerPetSkill old = oldSkill.get();
            old.setIsEquipped(false);
            old.setPosition(null);
            playerPetSkillRepository.save(old);
        }

        // 新技能装备到该位置
        newSkill.setIsEquipped(true);
        newSkill.setPosition(position);
    }

    /**
     * 装备技能到指定位置
     * @param playerPetId 玩家宠物ID
     * @param skillId 技能ID
     * @param position 位置 (0-3)
     */
    @Transactional
    public void equipSkill(Long playerPetId, Long skillId, int position) {
        if (position < 0 || position > 3) {
            throw new IllegalArgumentException("技能位置必须在0-3之间");
        }

        PlayerPetSkill skill = playerPetSkillRepository
                .findByPlayerPetIdAndSkillId(playerPetId, skillId)
                .orElseThrow(() -> new IllegalArgumentException("宠物未学会该技能"));

        // 如果该位置已有技能，先卸下
        Optional<PlayerPetSkill> oldSkill = playerPetSkillRepository
                .findByPlayerPetIdAndPosition(playerPetId, position);

        if (oldSkill.isPresent() && !oldSkill.get().getId().equals(skill.getId())) {
            PlayerPetSkill old = oldSkill.get();
            old.setIsEquipped(false);
            old.setPosition(null);
            playerPetSkillRepository.save(old);
        }

        // 装备新技能
        skill.setIsEquipped(true);
        skill.setPosition(position);
        playerPetSkillRepository.save(skill);

        log.info("宠物{}在位置{}装备了技能{}", playerPetId, position, skillId);
    }

    /**
     * 卸下技能
     */
    @Transactional
    public void unequipSkill(Long playerPetId, Long skillId) {
        PlayerPetSkill skill = playerPetSkillRepository
                .findByPlayerPetIdAndSkillId(playerPetId, skillId)
                .orElseThrow(() -> new IllegalArgumentException("宠物未学会该技能"));

        skill.setIsEquipped(false);
        skill.setPosition(null);
        playerPetSkillRepository.save(skill);

        log.info("宠物{}卸下了技能{}", playerPetId, skillId);
    }

    /**
     * 获取宠物已学会的所有技能
     */
    public List<PlayerPetSkill> getLearnedSkills(Long playerPetId) {
        return playerPetSkillRepository.findByPlayerPetId(playerPetId);
    }

    /**
     * 获取宠物已装备的技能
     */
    public List<PlayerPetSkill> getEquippedSkills(Long playerPetId) {
        return playerPetSkillRepository.findByPlayerPetIdAndIsEquippedTrue(playerPetId);
    }

    /**
     * 恢复所有技能的PP
     */
    @Transactional
    public void restoreAllPp(Long playerPetId) {
        List<PlayerPetSkill> skills = playerPetSkillRepository.findByPlayerPetId(playerPetId);
        skills.forEach(PlayerPetSkill::restorePp);
        playerPetSkillRepository.saveAll(skills);
        log.info("恢复宠物{}所有技能的PP", playerPetId);
    }
}
