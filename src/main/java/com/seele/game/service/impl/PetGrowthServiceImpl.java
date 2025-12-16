package com.seele.game.service.impl;

import com.seele.game.entity.PetTemplate;
import com.seele.game.entity.PlayerPet;
import com.seele.game.enums.StatType;
import com.seele.game.mapper.PetTemplateMapper;
import com.seele.game.mapper.PlayerPetMapper;
import com.seele.game.service.IPetGrowthService;
import com.seele.game.service.ISkillLearnService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 宠物成长服务实现
 * 负责经验值、升级、属性计算等
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PetGrowthServiceImpl implements IPetGrowthService {

    private final PlayerPetMapper playerPetMapper;
    private final PetTemplateMapper petTemplateMapper;
    private final ISkillLearnService skillLearnService;

    @Override
    public int calculateExpForLevel(int level) {
        if (level <= 1) return 0;
        return (int) Math.pow(level, 3);
    }

    @Override
    public int calculateExpBetweenLevels(int fromLevel, int toLevel) {
        return calculateExpForLevel(toLevel) - calculateExpForLevel(fromLevel);
    }

    @Override
    @Transactional
    public boolean addExp(PlayerPet playerPet, int expGained) {
        if (playerPet.getLevel() >= 100) {
            log.info("宠物{}已达到满级，不再获得经验", playerPet.getId());
            return false;
        }

        playerPet.setExp(playerPet.getExp() + expGained);
        boolean leveledUp = false;

        // 检查是否升级（可能连续升多级）
        while (playerPet.getExp() >= playerPet.getNextLevelExp() && playerPet.getLevel() < 100) {
            levelUp(playerPet);
            leveledUp = true;
        }

        playerPetMapper.updateById(playerPet);
        return leveledUp;
    }

    /**
     * 宠物升级
     */
    private void levelUp(PlayerPet playerPet) {
        int oldLevel = playerPet.getLevel();
        int newLevel = oldLevel + 1;

        playerPet.setLevel(newLevel);
        playerPet.setNextLevelExp(calculateExpForLevel(newLevel + 1));

        // 重新计算所有属性
        recalculateStats(playerPet);

        // 完全恢复HP
        playerPet.setCurrentHp(playerPet.getMaxHp());

        log.info("宠物{}从{}级升到{}级", playerPet.getId(), oldLevel, newLevel);

        // 检查是否学会新技能
        skillLearnService.checkAndLearnSkillsOnLevelUp(playerPet, newLevel);
    }

    @Override
    @Transactional
    public void recalculateStats(PlayerPet playerPet) {
        PetTemplate template = petTemplateMapper.selectById(playerPet.getPetTemplateId());
        if (template == null) {
            throw new IllegalArgumentException("宠物模板不存在");
        }

        int level = playerPet.getLevel();

        // 计算各项属性
        playerPet.setMaxHp(calculateStat(template, playerPet, StatType.HP, level));
        playerPet.setAttack(calculateStat(template, playerPet, StatType.ATTACK, level));
        playerPet.setDefense(calculateStat(template, playerPet, StatType.DEFENSE, level));
        playerPet.setMagicAttack(calculateStat(template, playerPet, StatType.MAGIC_ATTACK, level));
        playerPet.setMagicDefense(calculateStat(template, playerPet, StatType.MAGIC_DEFENSE, level));
        playerPet.setSpeed(calculateStat(template, playerPet, StatType.SPEED, level));

        // 如果当前HP超过最大HP，调整为最大HP
        if (playerPet.getCurrentHp() > playerPet.getMaxHp()) {
            playerPet.setCurrentHp(playerPet.getMaxHp());
        }
    }

    /**
     * 计算单项属性
     * 公式：属性 = 基础值 + (成长率 × 等级) + IV
     * HP额外有10%加成
     */
    private int calculateStat(PetTemplate template, PlayerPet pet, StatType statType, int level) {
        int baseValue = getBaseStat(template, statType);
        double growthRate = getGrowthRate(template, statType);
        int iv = getIV(pet, statType);

        double rawStat = baseValue + (growthRate * level) + iv;

        // HP有额外加成
        if (statType == StatType.HP) {
            rawStat *= 1.1;
        }

        return Math.max(1, (int) rawStat);
    }

    /**
     * 获取模板的基础属性值
     */
    private int getBaseStat(PetTemplate template, StatType statType) {
        return switch (statType) {
            case HP -> template.getBaseHp();
            case ATTACK -> template.getBaseAttack();
            case DEFENSE -> template.getBaseDefense();
            case MAGIC_ATTACK -> template.getBaseMagicAttack();
            case MAGIC_DEFENSE -> template.getBaseMagicDefense();
            case SPEED -> template.getBaseSpeed();
        };
    }

    /**
     * 获取模板的成长率
     */
    private double getGrowthRate(PetTemplate template, StatType statType) {
        return switch (statType) {
            case HP -> template.getHpGrowth();
            case ATTACK -> template.getAttackGrowth();
            case DEFENSE -> template.getDefenseGrowth();
            case MAGIC_ATTACK -> template.getMagicAttackGrowth();
            case MAGIC_DEFENSE -> template.getMagicDefenseGrowth();
            case SPEED -> template.getSpeedGrowth();
        };
    }

    /**
     * 获取宠物的个体值
     */
    private int getIV(PlayerPet pet, StatType statType) {
        return switch (statType) {
            case HP -> pet.getIvHp();
            case ATTACK -> pet.getIvAttack();
            case DEFENSE -> pet.getIvDefense();
            case MAGIC_ATTACK -> pet.getIvMagicAttack();
            case MAGIC_DEFENSE -> pet.getIvMagicDefense();
            case SPEED -> pet.getIvSpeed();
        };
    }

    @Override
    public int generateRandomIV() {
        return (int) (Math.random() * 32);
    }

    @Override
    public int[] generateRandomIVs() {
        return new int[]{
                generateRandomIV(), // HP
                generateRandomIV(), // Attack
                generateRandomIV(), // Defense
                generateRandomIV(), // Magic Attack
                generateRandomIV(), // Magic Defense
                generateRandomIV()  // Speed
        };
    }
}
