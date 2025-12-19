package com.seele.game.entity;

import lombok.*;

import java.time.LocalDateTime;

/**
 * 玩家队伍
 * 玩家可以组建队伍（最多6只宠物）
 */
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlayerTeam {

    private Long id;

    /**
     * 玩家ID
     */
    private Long playerId;

    /**
     * 队伍位置 (1-6)
     */
    private Integer position;

    /**
     * 宠物ID（可以为空表示该位置没有宠物）
     */
    private Long playerPetId;

    /**
     * 队伍名称
     */
    private String teamName;

    /**
     * 是否为当前使用的队伍
     */
    private Boolean isActive;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
