package com.seele.game.service;

import com.seele.game.entity.PetTemplate;
import com.seele.game.entity.PlayerPet;
import com.seele.game.entity.PlayerTeam;

import java.util.List;

/**
 * 宠物管理服务接口
 * 负责宠物获取、队伍管理等
 */
public interface IPetManagementService {

    /**
     * 获取所有初始宠物模板
     */
    List<PetTemplate> getStarterPets();

    /**
     * 玩家选择初始宠物
     * @param playerId 玩家ID
     * @param petTemplateId 宠物模板ID
     * @param nickname 昵称
     * @return 创建的玩家宠物
     */
    PlayerPet chooseStarterPet(Long playerId, Long petTemplateId, String nickname);

    /**
     * 创建宠物实例
     * @param playerId 玩家ID
     * @param petTemplateId 宠物模板ID
     * @param nickname 昵称
     * @param level 初始等级
     * @return 创建的宠物
     */
    PlayerPet createPet(Long playerId, Long petTemplateId, String nickname, int level);

    /**
     * 添加宠物到队伍
     * @param playerId 玩家ID
     * @param playerPetId 宠物ID
     * @param position 位置 (1-6)
     */
    void addToTeam(Long playerId, Long playerPetId, int position);

    /**
     * 从队伍移除宠物
     */
    void removeFromTeam(Long playerId, int position);

    /**
     * 获取玩家的队伍
     */
    List<PlayerTeam> getPlayerTeam(Long playerId);

    /**
     * 根据ID获取单个宠物
     */
    PlayerPet getPetById(Long petId);

    /**
     * 获取玩家的所有宠物
     */
    List<PlayerPet> getPlayerPets(Long playerId);

    /**
     * 获取玩家所有未昏厥的宠物
     */
    List<PlayerPet> getActivePets(Long playerId);

    /**
     * 治疗宠物
     */
    void healPet(Long playerPetId);

    /**
     * 治疗玩家的所有宠物
     */
    void healAllPets(Long playerId);
}
