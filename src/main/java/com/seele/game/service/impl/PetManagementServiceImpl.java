package com.seele.game.service.impl;

import com.seele.game.entity.PetTemplate;
import com.seele.game.entity.PlayerPet;
import com.seele.game.entity.PlayerTeam;
import com.seele.game.enums.PetStatus;
import com.seele.game.mapper.PetTemplateMapper;
import com.seele.game.mapper.PlayerPetMapper;
import com.seele.game.mapper.PlayerTeamMapper;
import com.seele.game.service.IPetGrowthService;
import com.seele.game.service.IPetManagementService;
import com.seele.game.service.ISkillLearnService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 宠物管理服务实现
 * 负责宠物获取、队伍管理等
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PetManagementServiceImpl implements IPetManagementService {

    private final PetTemplateMapper petTemplateMapper;
    private final PlayerPetMapper playerPetMapper;
    private final PlayerTeamMapper playerTeamMapper;
    private final IPetGrowthService petGrowthService;
    private final ISkillLearnService skillLearnService;

    @Override
    public List<PetTemplate> getStarterPets() {
        return petTemplateMapper.findByIsStarterTrue();
    }

    @Override
    public List<PetTemplate> getAllPetTemplates() {
        return petTemplateMapper.findAll();
    }

    @Override
    @Transactional
    public PlayerPet chooseStarterPet(Long playerId, Long petTemplateId, String nickname) {
        PetTemplate template = petTemplateMapper.selectById(petTemplateId);
        if (template == null) {
            throw new IllegalArgumentException("宠物模板不存在");
        }

        if (!template.getIsStarter()) {
            throw new IllegalArgumentException("该宠物不是初始宠物");
        }

        // 检查玩家是否已有宠物
        long petCount = playerPetMapper.countByPlayerId(playerId);
        if (petCount > 0) {
            throw new IllegalArgumentException("玩家已有宠物，不能重复选择初始宠物");
        }

        // 创建宠物实例
        PlayerPet pet = createPet(playerId, petTemplateId, nickname, 5);

        // 学习初始技能（1级和5级的技能）
        skillLearnService.checkAndLearnSkillsOnLevelUp(pet, 1);
        skillLearnService.checkAndLearnSkillsOnLevelUp(pet, 5);

        // 添加到队伍第一位
        addToTeam(playerId, pet.getId(), 1);

        log.info("玩家{}选择了初始宠物{}，ID为{}", playerId, template.getName(), pet.getId());
        return pet;
    }

    @Override
    @Transactional
    public PlayerPet createPet(Long playerId, Long petTemplateId, String nickname, int level) {
        PetTemplate template = petTemplateMapper.selectById(petTemplateId);
        if (template == null) {
            throw new IllegalArgumentException("宠物模板不存在");
        }

        PlayerPet pet = new PlayerPet();
        pet.setPlayerId(playerId);
        pet.setPetTemplateId(petTemplateId);
        pet.setNickname(nickname != null ? nickname : template.getName());
        pet.setLevel(level);
        pet.setExp(petGrowthService.calculateExpForLevel(level));
        pet.setNextLevelExp(petGrowthService.calculateExpForLevel(level + 1));

        // 生成随机个体值
        int[] ivs = petGrowthService.generateRandomIVs();
        pet.setIvHp(ivs[0]);
        pet.setIvAttack(ivs[1]);
        pet.setIvDefense(ivs[2]);
        pet.setIvMagicAttack(ivs[3]);
        pet.setIvMagicDefense(ivs[4]);
        pet.setIvSpeed(ivs[5]);

        // 计算所有属性（使用正确的公式：基础值 + 成长率×等级 + IV）
        petGrowthService.recalculateStats(pet);

        // 设置满HP
        pet.setCurrentHp(pet.getMaxHp());

        // 设置其他初始属性
        pet.setStatusCondition(PetStatus.NORMAL);
        pet.setFriendship(50);
        pet.setCreatedAt(java.time.LocalDateTime.now());
        pet.setUpdatedAt(java.time.LocalDateTime.now());

        // 一次性插入正确的数据
        playerPetMapper.insert(pet);

        return pet;
    }

    @Override
    @Transactional
    public void addToTeam(Long playerId, Long playerPetId, int position) {
        if (position < 1 || position > 6) {
            throw new IllegalArgumentException("队伍位置必须在1-6之间");
        }

        // 检查宠物是否属于该玩家
        PlayerPet pet = playerPetMapper.selectById(playerPetId);
        if (pet == null) {
            throw new IllegalArgumentException("宠物不存在");
        }

        if (!pet.getPlayerId().equals(playerId)) {
            throw new IllegalArgumentException("该宠物不属于当前玩家");
        }

        // 检查该位置是否已有宠物
        PlayerTeam teamSlot = playerTeamMapper.findByPlayerIdAndPosition(playerId, position);
        boolean isNewSlot = (teamSlot == null);
        if (isNewSlot) {
            teamSlot = new PlayerTeam();
        }

        teamSlot.setPlayerId(playerId);
        teamSlot.setPosition(position);
        teamSlot.setPlayerPetId(playerPetId);

        // 如果是第一只宠物，设为激活队伍
        long teamCount = playerTeamMapper.findByPlayerIdOrderByPosition(playerId).size();
        if (teamCount == 0) {
            teamSlot.setIsActive(true);
        }

        if (isNewSlot) {
            playerTeamMapper.insert(teamSlot);
        } else {
            playerTeamMapper.updateById(teamSlot);
        }
        log.info("将宠物{}添加到玩家{}的队伍位置{}", playerPetId, playerId, position);
    }

    @Override
    @Transactional
    public void removeFromTeam(Long playerId, int position) {
        PlayerTeam teamSlot = playerTeamMapper.findByPlayerIdAndPosition(playerId, position);
        if (teamSlot == null) {
            throw new IllegalArgumentException("该位置没有宠物");
        }

        teamSlot.setPlayerPetId(null);
        playerTeamMapper.updateById(teamSlot);
        log.info("从玩家{}的队伍位置{}移除宠物", playerId, position);
    }

    @Override
    public List<PlayerTeam> getPlayerTeam(Long playerId) {
        return playerTeamMapper.findByPlayerIdOrderByPosition(playerId);
    }

    @Override
    public PlayerPet getPetById(Long petId) {
        PlayerPet pet = playerPetMapper.selectById(petId);
        if (pet == null) {
            throw new IllegalArgumentException("宠物不存在: " + petId);
        }
        return pet;
    }

    @Override
    public List<PlayerPet> getPlayerPets(Long playerId) {
        return playerPetMapper.findByPlayerId(playerId);
    }

    @Override
    public List<PlayerPet> getActivePets(Long playerId) {
        return playerPetMapper.findActivePetsByPlayerId(playerId);
    }

    @Override
    @Transactional
    public void healPet(Long playerPetId) {
        PlayerPet pet = playerPetMapper.selectById(playerPetId);
        if (pet == null) {
            throw new IllegalArgumentException("宠物不存在");
        }

        pet.fullRestore();
        playerPetMapper.updateById(pet);

        // 恢复所有技能PP
        skillLearnService.restoreAllPp(playerPetId);

        log.info("完全治疗宠物{}", playerPetId);
    }

    @Override
    @Transactional
    public void healAllPets(Long playerId) {
        List<PlayerPet> pets = playerPetMapper.findByPlayerId(playerId);
        pets.forEach(pet -> {
            pet.fullRestore();
            skillLearnService.restoreAllPp(pet.getId());
            playerPetMapper.updateById(pet);
        });
        log.info("完全治疗玩家{}的所有宠物", playerId);
    }
}
