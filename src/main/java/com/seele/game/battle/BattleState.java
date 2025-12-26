package com.seele.game.battle;

import com.seele.game.enums.BattleStatus;
import com.seele.game.enums.BattleType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 战斗状态（纯内存对象）
 * 支持PVE和PVP两种模式
 */
@Data
public class BattleState {
    // 战斗ID
    private String battleId;

    // 战斗类型（PVE或PVP）
    private BattleType battleType;

    // 玩家ID
    private Long player1Id;  // PVE时为玩家，PVP时为玩家1
    private Long player2Id;  // PVP时为玩家2，PVE时为null

    // 战斗状态
    private BattleStatus status;

    // 当前回合数
    private int currentTurn;

    // 双方宠物状态（单宠物模式，向后兼容）
    private BattlePetState player1Pet;  // PVE时为玩家宠物，PVP时为玩家1宠物
    private BattlePetState player2Pet;  // PVE时为AI宠物，PVP时为玩家2宠物

    // 多宠物队伍模式
    private List<BattlePetState> player1Team;  // 玩家1的宠物队伍（最多6只）
    private List<BattlePetState> player2Team;  // 玩家2的宠物队伍（最多6只，PVE时为AI队伍）
    private int player1ActiveIndex;  // 当前出战宠物索引（默认0）
    private int player2ActiveIndex;  // 当前出战宠物索引（默认0）

    // AI训练师（仅PVE时使用）
    private AITrainer aiTrainer;

    // PVP技能选择缓存
    private Long player1SkillChoice;
    private Long player2SkillChoice;
    private boolean player1Ready;
    private boolean player2Ready;

    // 切换宠物选择缓存
    private Integer player1SwitchTo;  // 玩家1要切换到的宠物索引（null表示不切换）
    private Integer player2SwitchTo;  // 玩家2要切换到的宠物索引（null表示不切换）

    // 最近的行动历史（最多保留10回合）
    private List<BattleRoundResult> recentRounds;

    // 战斗开始时间
    private LocalDateTime startTime;

    // 战斗结束时间
    private LocalDateTime endTime;

    // 胜者
    private String winner;

    public BattleState() {
        this.recentRounds = new ArrayList<>();
        this.currentTurn = 0;
        this.status = BattleStatus.PREPARING;
        this.startTime = LocalDateTime.now();
    }

    /**
     * 添加回合结果
     */
    public void addRoundResult(BattleRoundResult result) {
        recentRounds.add(result);
        // 只保留最近10回合
        if (recentRounds.size() > 10) {
            recentRounds.remove(0);
        }
    }

    /**
     * 检查战斗是否超时（30分钟）
     */
    public boolean isInactive(int minutes) {
        if (status == BattleStatus.FINISHED) {
            return false;
        }
        return LocalDateTime.now().isAfter(startTime.plusMinutes(minutes));
    }

    /**
     * 结束战斗
     */
    public void endBattle(String winner) {
        this.status = BattleStatus.FINISHED;
        this.endTime = LocalDateTime.now();
        this.winner = winner;
    }

    /**
     * 检查战斗是否结束
     */
    public boolean isEnded() {
        return status == BattleStatus.FINISHED;
    }

    /**
     * 获取玩家ID（向后兼容PVE）
     */
    public Long getPlayerId() {
        return player1Id;
    }

    /**
     * 设置玩家ID（向后兼容PVE）
     */
    public void setPlayerId(Long playerId) {
        this.player1Id = playerId;
    }

    /**
     * 获取玩家宠物（向后兼容PVE）
     */
    public BattlePetState getPlayerPet() {
        return player1Pet;
    }

    /**
     * 设置玩家宠物（向后兼容PVE）
     */
    public void setPlayerPet(BattlePetState playerPet) {
        this.player1Pet = playerPet;
    }

    /**
     * 获取AI宠物（向后兼容PVE）
     */
    public BattlePetState getAiPet() {
        return player2Pet;
    }

    /**
     * 设置AI宠物（向后兼容PVE）
     */
    public void setAiPet(BattlePetState aiPet) {
        this.player2Pet = aiPet;
    }

    /**
     * 检查是否为PVP模式
     */
    public boolean isPvp() {
        return battleType == BattleType.PVP;
    }

    /**
     * 检查是否为PVE模式
     */
    public boolean isPve() {
        return battleType == BattleType.PVE;
    }

    /**
     * 重置技能选择（新回合开始）
     */
    public void resetSkillChoices() {
        this.player1SkillChoice = null;
        this.player2SkillChoice = null;
        this.player1SwitchTo = null;
        this.player2SwitchTo = null;
        this.player1Ready = false;
        this.player2Ready = false;
    }

    /**
     * 检查是否使用多宠物模式
     */
    public boolean isTeamMode() {
        return player1Team != null && !player1Team.isEmpty();
    }

    /**
     * 获取玩家1当前出战宠物
     */
    public BattlePetState getPlayer1ActivePet() {
        if (isTeamMode()) {
            return player1Team.get(player1ActiveIndex);
        }
        return player1Pet;
    }

    /**
     * 获取玩家2当前出战宠物
     */
    public BattlePetState getPlayer2ActivePet() {
        if (isTeamMode()) {
            return player2Team.get(player2ActiveIndex);
        }
        return player2Pet;
    }

    /**
     * 切换玩家1的宠物
     */
    public boolean switchPlayer1Pet(int newIndex) {
        if (!isTeamMode() || newIndex < 0 || newIndex >= player1Team.size()) {
            return false;
        }
        BattlePetState newPet = player1Team.get(newIndex);
        if (newPet.isFainted()) {
            return false;  // 不能切换到已阵亡的宠物
        }
        player1ActiveIndex = newIndex;
        return true;
    }

    /**
     * 切换玩家2的宠物
     */
    public boolean switchPlayer2Pet(int newIndex) {
        if (!isTeamMode() || newIndex < 0 || newIndex >= player2Team.size()) {
            return false;
        }
        BattlePetState newPet = player2Team.get(newIndex);
        if (newPet.isFainted()) {
            return false;  // 不能切换到已阵亡的宠物
        }
        player2ActiveIndex = newIndex;
        return true;
    }

    /**
     * 检查玩家1是否还有可用宠物
     */
    public boolean hasPlayer1AlivePets() {
        if (!isTeamMode()) {
            return !player1Pet.isFainted();
        }
        return player1Team.stream().anyMatch(pet -> !pet.isFainted());
    }

    /**
     * 检查玩家2是否还有可用宠物
     */
    public boolean hasPlayer2AlivePets() {
        if (!isTeamMode()) {
            return !player2Pet.isFainted();
        }
        return player2Team.stream().anyMatch(pet -> !pet.isFainted());
    }

    /**
     * 检查双方是否都已准备好
     */
    public boolean bothPlayersReady() {
        return player1Ready && player2Ready;
    }
}
