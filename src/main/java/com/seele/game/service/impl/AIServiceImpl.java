package com.seele.game.service.impl;

import com.seele.game.battle.BattlePetState;
import com.seele.game.battle.BattleState;
import com.seele.game.entity.*;
import com.seele.game.enums.AIDifficulty;
import com.seele.game.enums.PetRarity;
import com.seele.game.enums.SkillType;
import com.seele.game.mapper.PetLevelSkillMapper;
import com.seele.game.mapper.PetTemplateMapper;
import com.seele.game.service.IAIService;
import com.seele.game.service.IBattleCalculationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * AI决策服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIServiceImpl implements IAIService {

    private final PetTemplateMapper petTemplateMapper;
    private final PetLevelSkillMapper petLevelSkillMapper;
    private final IBattleCalculationService calculationService;
    private final com.seele.game.mapper.SkillMapper skillMapper;
    private final Random random = new Random();

    @Override
    public PlayerPet generateAIPet(PlayerPet playerPet, AIDifficulty difficulty) {
        // 获取所有可用的宠物模板（排除初始宠物可能更好，但暂时简化）
        List<PetTemplate> allTemplates = petTemplateMapper.findByIsStarterTrue();

        PetTemplate playerPetTemplate = petTemplateMapper.selectById(playerPet.getPetTemplateId());
        // 根据难度选择宠物模板
        PetTemplate template = selectTemplateByDifficulty(allTemplates, playerPetTemplate.getType(), difficulty);

        // 计算AI宠物等级（基于玩家宠物等级和难度）
        int aiLevel = calculateAILevel(playerPet.getLevel(), difficulty);

        // 创建AI宠物实例
        PlayerPet aiPet = new PlayerPet();
        aiPet.setId(-1L); // 临时ID，表示这是AI宠物
        aiPet.setPlayerId(-1L); // AI训练师ID
        aiPet.setPetTemplateId(template.getId());
        aiPet.setTemplate(template);
        aiPet.setNickname(template.getName());
        aiPet.setLevel(aiLevel);

        // 生成随机IV值
        aiPet.setIvHp(random.nextInt(32));
        aiPet.setIvAttack(random.nextInt(32));
        aiPet.setIvDefense(random.nextInt(32));
        aiPet.setIvMagicAttack(random.nextInt(32));
        aiPet.setIvMagicDefense(random.nextInt(32));
        aiPet.setIvSpeed(random.nextInt(32));

        // 计算属性
        aiPet.setMaxHp((int)(template.getBaseHp() + template.getHpGrowth() * aiLevel + aiPet.getIvHp()));
        aiPet.setCurrentHp(aiPet.getMaxHp());
        aiPet.setAttack((int)(template.getBaseAttack() + template.getAttackGrowth() * aiLevel + aiPet.getIvAttack()));
        aiPet.setDefense((int)(template.getBaseDefense() + template.getDefenseGrowth() * aiLevel + aiPet.getIvDefense()));
        aiPet.setMagicAttack((int)(template.getBaseMagicAttack() + template.getMagicAttackGrowth() * aiLevel + aiPet.getIvMagicAttack()));
        aiPet.setMagicDefense((int)(template.getBaseMagicDefense() + template.getMagicDefenseGrowth() * aiLevel + aiPet.getIvMagicDefense()));
        aiPet.setSpeed((int)(template.getBaseSpeed() + template.getSpeedGrowth() * aiLevel + aiPet.getIvSpeed()));

        // 生成AI宠物的技能（获取该等级应该学会的所有技能，取最近的4个）
        List<PetLevelSkill> levelSkills = petLevelSkillMapper.findByPetTemplateIdAndLearnLevelLessThanEqual(template.getId(), aiLevel);
        List<PlayerPetSkill> aiSkills = new java.util.ArrayList<>();

        // 取最近学会的4个技能
        int skillCount = Math.min(4, levelSkills.size());
        for (int i = levelSkills.size() - skillCount; i < levelSkills.size(); i++) {
            PetLevelSkill levelSkill = levelSkills.get(i);
            Skill skill = skillMapper.selectById(levelSkill.getSkillId());

            PlayerPetSkill petSkill = new PlayerPetSkill();
            petSkill.setPlayerPetId(-1L);
            petSkill.setSkillId(skill.getId());
            petSkill.setSkill(skill);
            petSkill.setCurrentPp(skill.getMaxPp());
            petSkill.setMaxPp(skill.getMaxPp());
            petSkill.setIsEquipped(true);
            petSkill.setPosition(aiSkills.size());

            aiSkills.add(petSkill);
        }

        aiPet.setSkills(aiSkills);

        return aiPet;
    }

    @Override
    public Skill selectSkill(BattleState battleState) {
        BattlePetState aiPet = battleState.getAiPet();
        BattlePetState playerPet = battleState.getPlayerPet();
        AIDifficulty difficulty = battleState.getAiTrainer().getDifficulty();

        // 获取所有可用的技能（PP > 0）
        List<Skill> availableSkills = getAvailableSkills(aiPet);

        if (availableSkills.isEmpty()) {
            // 如果没有可用技能，返回第一个技能（即使PP为0，后续会处理）
            return aiPet.getOriginalPet().getSkills().get(0).getSkill();
        }

        // 根据难度选择技能
        return switch (difficulty) {
            case EASY -> selectSkillEasy(availableSkills);
            case MEDIUM -> selectSkillMedium(availableSkills, aiPet, playerPet);
            case HARD -> selectSkillHard(availableSkills, aiPet, playerPet, difficulty);
        };
    }

    @Override
    public double evaluateSkill(Skill skill, BattlePetState attacker, BattlePetState defender, AIDifficulty difficulty) {
        double score = 0;

        // 基础威力评分
        score += skill.getPower();

        // 属性克制评分
        double effectiveness = calculationService.getTypeEffectiveness(attacker, defender, skill);
        score *= effectiveness;

        // 命中率评分
        score *= (skill.getAccuracy() / 100.0);

        // 困难难度下考虑更多因素
        if (difficulty == AIDifficulty.HARD) {
            // 如果对方HP较低，优先高威力技能
            if (defender.getCurrentHp() < defender.getMaxHp() * 0.3) {
                score *= 1.2;
            }

            // 如果自己HP较低，可能选择状态技能（暂时简化）
            if (skill.getSkillType() == SkillType.STATUS && attacker.getCurrentHp() < attacker.getMaxHp() * 0.5) {
                score += 20;
            }
        }

        return score;
    }

    /**
     * 简单AI：随机选择
     */
    private Skill selectSkillEasy(List<Skill> skills) {
        return skills.get(random.nextInt(skills.size()));
    }

    /**
     * 中等AI：优先克制属性和高威力技能
     */
    private Skill selectSkillMedium(List<Skill> skills, BattlePetState attacker, BattlePetState defender) {
        // 按威力排序，选择威力最高的
        return skills.stream()
                .max(Comparator.comparingInt(skill -> {
                    double effectiveness = calculationService.getTypeEffectiveness(attacker, defender, skill);
                    return (int) (skill.getPower() * effectiveness);
                }))
                .orElse(skills.get(0));
    }

    /**
     * 困难AI：综合评估选择最佳技能
     */
    private Skill selectSkillHard(List<Skill> skills, BattlePetState attacker, BattlePetState defender, AIDifficulty difficulty) {
        return skills.stream()
                .max(Comparator.comparingDouble(skill -> evaluateSkill(skill, attacker, defender, difficulty)))
                .orElse(skills.get(0));
    }

    /**
     * 获取可用技能（PP > 0）
     */
    private List<Skill> getAvailableSkills(BattlePetState aiPet) {
        return aiPet.getOriginalPet().getSkills().stream()
                .filter(skill -> aiPet.getSkillPpMap().getOrDefault(skill.getSkill().getId(), 0) > 0)
                .map(PlayerPetSkill::getSkill)
                .collect(Collectors.toList());
    }

    /**
     * 根据难度选择宠物模板
     */
    private PetTemplate selectTemplateByDifficulty(List<PetTemplate> templates, com.seele.game.enums.PetType playerType, AIDifficulty difficulty) {
        List<PetTemplate> filtered = templates;

        // 困难难度下，优先选择克制玩家的属性
        if (difficulty == AIDifficulty.HARD) {
            filtered = templates.stream()
                    .filter(t -> t.getType().getEffectiveness(playerType) >= 1.5)
                    .collect(Collectors.toList());

            // 如果没有克制的，就选择不被克的
            if (filtered.isEmpty()) {
                filtered = templates.stream()
                        .filter(t -> t.getType().getEffectiveness(playerType) >= 1.0)
                        .collect(Collectors.toList());
            }
        }

        // 如果还是空的，使用所有模板
        if (filtered.isEmpty()) {
            filtered = templates;
        }

        // 根据难度过滤稀有度
        if (difficulty == AIDifficulty.EASY) {
            // 简单难度：只选普通和优秀
            filtered = filtered.stream()
                    .filter(t -> t.getRarity() == PetRarity.COMMON || t.getRarity() == PetRarity.UNCOMMON)
                    .collect(Collectors.toList());
        } else if (difficulty == AIDifficulty.MEDIUM) {
            // 中等难度：排除传说
            filtered = filtered.stream()
                    .filter(t -> t.getRarity() != PetRarity.LEGENDARY)
                    .collect(Collectors.toList());
        }

        // 如果过滤后为空，使用原列表
        if (filtered.isEmpty()) {
            filtered = templates;
        }

        // 随机选择一个
        return filtered.get(random.nextInt(filtered.size()));
    }

    /**
     * 计算AI宠物等级
     */
    private int calculateAILevel(int playerLevel, AIDifficulty difficulty) {
        int aiLevel = (int) (playerLevel * difficulty.getLevelMultiplier());
        return Math.max(1, Math.min(100, aiLevel)); // 限制在1-100之间
    }
}
