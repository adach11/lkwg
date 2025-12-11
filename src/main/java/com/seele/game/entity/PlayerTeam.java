package com.seele.game.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 玩家队伍
 * 玩家可以组建队伍（最多6只宠物）
 */
@Entity
@Table(name = "player_team",
       uniqueConstraints = @UniqueConstraint(columnNames = {"player_id", "position"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerTeam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 玩家ID
     */
    @Column(nullable = false)
    private Long playerId;

    /**
     * 队伍位置 (1-6)
     */
    @Column(nullable = false)
    private Integer position;

    /**
     * 宠物ID（可以为空表示该位置没有宠物）
     */
    private Long playerPetId;

    /**
     * 队伍名称
     */
    @Column(length = 50)
    private String teamName;

    /**
     * 是否为当前使用的队伍
     */
    @Column(nullable = false)
    private Boolean isActive = false;

    /**
     * 创建时间
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
