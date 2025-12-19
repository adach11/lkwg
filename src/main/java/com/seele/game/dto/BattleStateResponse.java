package com.seele.game.dto;

import com.seele.game.enums.BattleStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 战斗状态响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BattleStateResponse {
    private String battleId;
    private BattleStatus status;
    private int currentTurn;

    // 玩家宠物信息
    private PetBattleInfo playerPet;

    // AI宠物信息
    private PetBattleInfo aiPet;

    // AI训练师名称
    private String aiTrainerName;

    // 可用技能列表
    private List<SkillInfo> availableSkills;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PetBattleInfo {
        private Long id;
        private String name;
        private int level;
        private String type;
        private int currentHp;
        private int maxHp;
        private String status;
        private boolean fainted;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkillInfo {
        private Long id;
        private String name;
        private String type;
        private int power;
        private int accuracy;
        private int currentPp;
        private int maxPp;
    }
}
