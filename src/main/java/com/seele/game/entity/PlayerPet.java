package com.seele.game.entity;

import com.seele.game.enums.PetStatus;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 玩家宠物实例 - 每个玩家拥有的具体宠物
 */
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlayerPet {

    private Long id;

    /**
     * 玩家ID
     */
    private Long playerId;

    /**
     * 宠物模板ID
     */
    private Long petTemplateId;

    /**
     * 宠物模板（关联查询）
     */
    private PetTemplate template;

    /**
     * 昵称（玩家自定义）
     */
    private String nickname;

    /**
     * 当前等级 (1-100)
     */
    private Integer level;

    /**
     * 当前经验值
     */
    private Integer exp;

    /**
     * 升到下一级所需经验值
     */
    private Integer nextLevelExp;

    /**
     * 个体值 (IV) - 范围 0-31
     * 用于增加宠物属性的随机性和独特性
     */
    private Integer ivHp;

    private Integer ivAttack;

    private Integer ivDefense;

    private Integer ivMagicAttack;

    private Integer ivMagicDefense;

    private Integer ivSpeed;

    /**
     * 当前HP（战斗中会变化）
     */
    private Integer currentHp;

    /**
     * 最大HP（根据等级和IV计算）
     */
    private Integer maxHp;

    /**
     * 攻击力（计算后的最终值）
     */
    private Integer attack;

    /**
     * 防御力
     */
    private Integer defense;

    /**
     * 特攻
     */
    private Integer magicAttack;

    /**
     * 特防
     */
    private Integer magicDefense;

    /**
     * 速度
     */
    private Integer speed;

    /**
     * 当前状态异常
     */
    private PetStatus statusCondition;

    /**
     * 已装备的技能列表（关联查询）
     */
    private java.util.List<PlayerPetSkill> skills;

    /**
     * 亲密度 (0-255)
     */
    private Integer friendship;

    /**
     * 获得时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 判断是否已昏厥
     */
    public boolean isFainted() {
        return currentHp <= 0;
    }

    /**
     * 恢复HP
     */
    public void heal(int amount) {
        currentHp = Math.min(currentHp + amount, maxHp);
    }

    /**
     * 受到伤害
     */
    public void takeDamage(int damage) {
        currentHp = Math.max(currentHp - damage, 0);
    }

    /**
     * 完全恢复
     */
    public void fullRestore() {
        currentHp = maxHp;
        statusCondition = PetStatus.NORMAL;
    }
}
