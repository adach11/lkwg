package com.seele.game.service.impl;

import com.seele.game.battle.BattleAction;
import com.seele.game.battle.BattlePetState;
import com.seele.game.entity.PetTemplate;
import com.seele.game.entity.Skill;
import com.seele.game.enums.PetStatus;
import com.seele.game.enums.SkillType;
import com.seele.game.enums.StatType;
import com.seele.game.service.IBattleCalculationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Random;

/**
 * 战斗计算服务实现
 */
@Slf4j
@Service
public class BattleCalculationServiceImpl implements IBattleCalculationService {

    private final Random random = new Random();

    @Override
    public int calculateDamage(BattlePetState attacker, BattlePetState defender, Skill skill, boolean isCritical) {
        // 变化类技能不造成伤害
        if (skill.getSkillType() == SkillType.STATUS) {
            return 0;
        }

        PetTemplate attackerTemplate = attacker.getOriginalPet().getTemplate();
        PetTemplate defenderTemplate = defender.getOriginalPet().getTemplate();

        int attackStat;
        int defenseStat;

        // 根据技能类型选择对应的攻防属性
        if (skill.getSkillType() == SkillType.PHYSICAL) {
            attackStat = attacker.getModifiedStat(StatType.ATTACK,
                    (int)(attackerTemplate.getBaseAttack() + attackerTemplate.getAttackGrowth() * attacker.getOriginalPet().getLevel()));
            defenseStat = defender.getModifiedStat(StatType.DEFENSE,
                    (int)(defenderTemplate.getBaseDefense() + defenderTemplate.getDefenseGrowth() * defender.getOriginalPet().getLevel()));
        } else { // 魔法攻击
            attackStat = attacker.getModifiedStat(StatType.MAGIC_ATTACK,
                    (int)(attackerTemplate.getBaseMagicAttack() + attackerTemplate.getMagicAttackGrowth() * attacker.getOriginalPet().getLevel()));
            defenseStat = defender.getModifiedStat(StatType.MAGIC_DEFENSE,
                    (int)(defenderTemplate.getBaseMagicDefense() + defenderTemplate.getMagicDefenseGrowth() * defender.getOriginalPet().getLevel()));
        }

        // 基础伤害公式
        double baseDamage = ((double) attackStat / defenseStat) * skill.getPower() * 0.4 + 2;

        // 属性克制倍率
        double typeEffectiveness = getTypeEffectiveness(attacker, defender, skill);

        // 随机系数 (0.85 - 1.0)
        double randomFactor = 0.85 + random.nextDouble() * 0.15;

        // 暴击倍率（使用传入的判定结果）
        double criticalMultiplier = isCritical ? 2.0 : 1.0;

        // 最终伤害
        int finalDamage = (int) (baseDamage * typeEffectiveness * randomFactor * criticalMultiplier);

        // 至少造成1点伤害（如果不是免疫）
        if (typeEffectiveness > 0 && finalDamage < 1) {
            finalDamage = 1;
        }

        log.debug("伤害计算: 基础={}, 克制={}, 随机={}, 暴击={}, 最终={}",
                baseDamage, typeEffectiveness, randomFactor, criticalMultiplier, finalDamage);

        return finalDamage;
    }

    @Override
    public boolean checkHit(Skill skill) {
        int accuracy = skill.getAccuracy();
        int roll = random.nextInt(100);
        return roll < accuracy;
    }

    @Override
    public boolean checkCritical() {
        // 6.25%的暴击率
        return random.nextInt(16) == 0;
    }

    @Override
    public int getEffectiveSpeed(BattlePetState pet) {
        PetTemplate template = pet.getOriginalPet().getTemplate();
        int baseSpeed = (int)(template.getBaseSpeed() + template.getSpeedGrowth() * pet.getOriginalPet().getLevel());
        return pet.getModifiedStat(StatType.SPEED, baseSpeed);
    }

    //克制关系
    @Override
    public double getTypeEffectiveness(BattlePetState attacker, BattlePetState defender, Skill skill) {
        // 使用技能的属性，如果技能没有属性则使用宠物的属性
        return skill.getPetType() != null ?
                skill.getPetType().getEffectiveness(defender.getOriginalPet().getTemplate().getType()) :
                attacker.getOriginalPet().getTemplate().getType().getEffectiveness(
                        defender.getOriginalPet().getTemplate().getType());
    }

    @Override
    public String processEndOfTurnStatus(BattlePetState pet) {
        PetStatus status = pet.getStatusCondition();
        if (status == null || status == PetStatus.NORMAL) {
            return null;
        }

        String petName = pet.getOriginalPet().getNickname();

        switch (status) {
            case BURN:
                int burnDamage = pet.getMaxHp() / 8;
                pet.takeDamage(burnDamage);
                return String.format("%s受到了烧伤伤害，损失了%d HP", petName, burnDamage);

            case POISON:
                int poisonDamage = pet.getMaxHp() / 8;
                pet.takeDamage(poisonDamage);
                return String.format("%s受到了中毒伤害，损失了%d HP", petName, poisonDamage);

            case FREEZE:
                // 10%几率解冻
                if (random.nextInt(10) == 0) {
                    pet.setStatusCondition(PetStatus.NORMAL);
                    return String.format("%s解除了冰冻状态！", petName);
                }
                return null;

            case SLEEP:
                // 33%几率醒来
                if (random.nextInt(3) == 0) {
                    pet.setStatusCondition(PetStatus.NORMAL);
                    return String.format("%s醒来了！", petName);
                }
                return null;

            default:
                return null;
        }
    }

    @Override
    public String checkCanAct(BattlePetState pet) {
        PetStatus status = pet.getStatusCondition();
        if (status == null || status == PetStatus.NORMAL) {
            return null;
        }

        String petName = pet.getOriginalPet().getNickname();

        switch (status) {
            case PARALYSIS:
                // 25%几率无法行动
                if (random.nextInt(4) == 0) {
                    return String.format("%s因麻痹无法行动！", petName);
                }
                return null;

            case FREEZE:
                return String.format("%s被冰冻了，无法行动！", petName);

            case SLEEP:
                return String.format("%s正在睡觉，无法行动！", petName);

            default:
                return null;
        }
    }

    @Override
    public boolean determineFirstMove(BattlePetState pet1, BattleAction action1, BattlePetState pet2, BattleAction action2) {
        // 获取动作优先度
        int priority1 = action1.getPriority();
        int priority2 = action2.getPriority();

        // 1. 优先级高的先行动
        if (priority1 != priority2) {
            return priority1 > priority2;
        }

        // 2. 优先级相同时，比较速度
        int speed1 = getEffectiveSpeed(pet1);
        int speed2 = getEffectiveSpeed(pet2);

        if (speed1 != speed2) {
            return speed1 > speed2;
        }

        // 3. 速度也相同时，随机决定
        return random.nextBoolean();
    }
}
