package com.seele.game.enums;

/**
 * 技能类型
 */
public enum SkillType {
    PHYSICAL("物理"),    // 使用攻击和防御计算伤害
    MAGIC("魔法"),       // 使用魔攻和魔抗计算伤害
    STATUS("变化");      // 不造成伤害，仅施加状态效果

    private final String displayName;

    SkillType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
