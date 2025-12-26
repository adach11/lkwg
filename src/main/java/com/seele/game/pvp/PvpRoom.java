package com.seele.game.pvp;

import com.seele.game.entity.PlayerPet;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * PVP房间
 */
@Data
public class PvpRoom {
    /**
     * 房间ID
     */
    private String roomId;

    /**
     * 房间状态
     */
    private RoomStatus status;

    /**
     * 玩家1 ID
     */
    private Long player1Id;

    /**
     * 玩家1宠物
     */
    private PlayerPet player1Pet;

    /**
     * 玩家2 ID
     */
    private Long player2Id;

    /**
     * 玩家2宠物
     */
    private PlayerPet player2Pet;

    /**
     * 战斗ID（战斗开始后生成）
     */
    private String battleId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 房间状态枚举
     */
    public enum RoomStatus {
        WAITING,    // 等待玩家2
        FULL,       // 房间已满，准备开始
        BATTLE,     // 战斗中
        FINISHED    // 已结束
    }

    public PvpRoom(String roomId, Long player1Id, PlayerPet player1Pet) {
        this.roomId = roomId;
        this.player1Id = player1Id;
        this.player1Pet = player1Pet;
        this.status = RoomStatus.WAITING;
        this.createTime = LocalDateTime.now();
    }

    /**
     * 玩家2加入
     */
    public void joinPlayer2(Long player2Id, PlayerPet player2Pet) {
        this.player2Id = player2Id;
        this.player2Pet = player2Pet;
        this.status = RoomStatus.FULL;
    }

    /**
     * 开始战斗
     */
    public void startBattle(String battleId) {
        this.battleId = battleId;
        this.status = RoomStatus.BATTLE;
    }

    /**
     * 结束战斗
     */
    public void finishBattle() {
        this.status = RoomStatus.FINISHED;
    }

    /**
     * 检查房间是否已满
     */
    public boolean isFull() {
        return player2Id != null;
    }

    /**
     * 检查是否超时（10分钟无人加入）
     */
    public boolean isTimeout() {
        if (status != RoomStatus.WAITING) {
            return false;
        }
        return LocalDateTime.now().isAfter(createTime.plusMinutes(10));
    }
}
