package com.seele.game.battle;

import com.seele.game.entity.PlayerPet;
import com.seele.game.entity.PlayerPetSkill;
import com.seele.game.enums.PetStatus;
import com.seele.game.enums.StatType;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 战斗中的宠物状态（纯内存对象）
 */
@Data
public class BattlePetState {
    // 原始宠物数据引用
    private PlayerPet originalPet;

    // 当前战斗属性
    private int currentHp;
    private int maxHp;

    // 技能PP值映射 (技能ID -> 当前PP)
    private Map<Long, Integer> skillPpMap;

    // 状态异常
    private PetStatus statusCondition;
    private int statusTurns; // 状态剩余回合数（用于睡眠）

    // 属性变化 (攻击+1、防御-1等，范围 -6 到 +6)
    private Map<StatType, Integer> statChanges;

    // 是否阵亡
    private boolean fainted;

    /**
     * 从玩家宠物创建战斗状态
     */
    public static BattlePetState fromPlayerPet(PlayerPet pet, List<PlayerPetSkill> skills) {
        BattlePetState state = new BattlePetState();
        state.setOriginalPet(pet);
        state.setCurrentHp(pet.getCurrentHp());
        state.setMaxHp(pet.getMaxHp());
        state.setStatusCondition(pet.getStatusCondition() != null ? pet.getStatusCondition() : PetStatus.NORMAL);
        state.setStatusTurns(0);
        state.setFainted(false);

        // 初始化技能PP
        Map<Long, Integer> ppMap = new HashMap<>();
        for (PlayerPetSkill skill : skills) {
            ppMap.put(skill.getSkill().getId(), skill.getSkill().getMaxPp());
        }
        state.setSkillPpMap(ppMap);

        // 初始化属性变化（全部为0）
        Map<StatType, Integer> changes = new HashMap<>();
        for (StatType type : StatType.values()) {
            changes.put(type, 0);
        }
        state.setStatChanges(changes);

        return state;
    }

    /**
     * 获取修正后的属性值
     */
    public int getModifiedStat(StatType statType, int baseStat) {
        int stage = statChanges.getOrDefault(statType, 0);
        double multiplier = stage >= 0 ? (2.0 + stage) / 2.0 : 2.0 / (2.0 - stage);

        // 麻痹状态下速度减半
//        if (statType == StatType.SPEED && statusCondition == PetStatus.PARALYSIS) {
//            multiplier *= 0.5;
//        }

        // 烧伤状态下物攻减半
//        if (statType == StatType.ATTACK && statusCondition == PetStatus.BURN) {
//            multiplier *= 0.5;
//        }

        return (int) (baseStat * multiplier);
    }

    /**
     * 受到伤害
     */
    public void takeDamage(int damage) {
        currentHp = Math.max(0, currentHp - damage);
        if (currentHp == 0) {
            fainted = true;
        }
    }

    /**
     * 恢复HP
     */
    public void heal(int amount) {
        if (!fainted) {
            currentHp = Math.min(maxHp, currentHp + amount);
        }
    }

    /**
     * 消耗技能PP
     */
    public boolean consumePP(Long skillId) {
        Integer currentPP = skillPpMap.get(skillId);
        if (currentPP == null || currentPP <= 0) {
            return false;
        }
        skillPpMap.put(skillId, currentPP - 1);
        return true;
    }

    /**
     * 修改属性等级
     */
    public void modifyStatStage(StatType statType, int change) {
        int current = statChanges.getOrDefault(statType, 0);
        int newValue = Math.max(-6, Math.min(6, current + change));
        statChanges.put(statType, newValue);
    }

    /**
     * 重置属性变化
     */
    public void resetStatChanges() {
        for (StatType type : StatType.values()) {
            statChanges.put(type, 0);
        }
    }
}
