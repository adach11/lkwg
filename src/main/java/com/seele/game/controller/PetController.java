package com.seele.game.controller;

import com.seele.game.dto.AddExpRequest;
import com.seele.game.dto.ApiResponse;
import com.seele.game.dto.PetCreateRequest;
import com.seele.game.entity.PetTemplate;
import com.seele.game.entity.PlayerPet;
import com.seele.game.entity.PlayerPetSkill;
import com.seele.game.service.IPetGrowthService;
import com.seele.game.service.IPetManagementService;
import com.seele.game.service.ISkillLearnService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 宠物管理接口
 */
@RestController
@RequestMapping("/api/pets")
@RequiredArgsConstructor
public class PetController {

    private final IPetManagementService petManagementService;
    private final IPetGrowthService petGrowthService;
    private final ISkillLearnService skillLearnService;

    /**
     * 获取所有初始宠物
     */
    @GetMapping("/starters")
    public ApiResponse<List<PetTemplate>> getStarterPets() {
        return ApiResponse.success(petManagementService.getStarterPets());
    }

    /**
     * 选择初始宠物
     */
    @PostMapping("/choose-starter")
    public ApiResponse<PlayerPet> chooseStarterPet(@Valid @RequestBody PetCreateRequest request) {
        try {
            PlayerPet pet = petManagementService.chooseStarterPet(
                    request.getPlayerId(),
                    request.getPetTemplateId(),
                    request.getNickname()
            );
            return ApiResponse.success("成功选择初始宠物", pet);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    /**
     * 获取玩家的所有宠物
     */
    @GetMapping("/player/{playerId}")
    public ApiResponse<List<PlayerPet>> getPlayerPets(@PathVariable Long playerId) {
        return ApiResponse.success(petManagementService.getPlayerPets(playerId));
    }

    /**
     * 获取宠物详情
     */
    @GetMapping("/{petId}")
    public ApiResponse<PlayerPet> getPetDetail(@PathVariable Long petId) {
        // TODO: 实现获取宠物详情
        return ApiResponse.success(null);
    }

    /**
     * 增加经验值
     */
    @PostMapping("/add-exp")
    public ApiResponse<PlayerPet> addExp(@Valid @RequestBody AddExpRequest request) {
        try {
            PlayerPet pet = petManagementService.getPlayerPets(1L).stream()
                    .filter(p -> p.getId().equals(request.getPlayerPetId()))
                    .findFirst()
                    .orElse(null);

            if (pet == null) {
                throw new IllegalArgumentException("宠物不存在");
            }

            boolean leveledUp = petGrowthService.addExp(pet, request.getExp());
            String message = leveledUp ? "获得经验并升级了！" : "获得经验值";

            return ApiResponse.success(message, pet);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    /**
     * 获取宠物已学会的技能
     */
    @GetMapping("/{petId}/skills")
    public ApiResponse<List<PlayerPetSkill>> getPetSkills(@PathVariable Long petId) {
        return ApiResponse.success(skillLearnService.getLearnedSkills(petId));
    }

    /**
     * 获取宠物已装备的技能
     */
    @GetMapping("/{petId}/skills/equipped")
    public ApiResponse<List<PlayerPetSkill>> getEquippedSkills(@PathVariable Long petId) {
        return ApiResponse.success(skillLearnService.getEquippedSkills(petId));
    }

    /**
     * 装备技能
     */
    @PostMapping("/{petId}/skills/{skillId}/equip")
    public ApiResponse<String> equipSkill(
            @PathVariable Long petId,
            @PathVariable Long skillId,
            @RequestParam int position) {
        try {
            skillLearnService.equipSkill(petId, skillId, position);
            return ApiResponse.success("技能装备成功", null);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    /**
     * 卸下技能
     */
    @PostMapping("/{petId}/skills/{skillId}/unequip")
    public ApiResponse<String> unequipSkill(
            @PathVariable Long petId,
            @PathVariable Long skillId) {
        try {
            skillLearnService.unequipSkill(petId, skillId);
            return ApiResponse.success("技能卸下成功", null);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    /**
     * 治疗宠物
     */
    @PostMapping("/{petId}/heal")
    public ApiResponse<String> healPet(@PathVariable Long petId) {
        try {
            petManagementService.healPet(petId);
            return ApiResponse.success("宠物已完全恢复", null);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    /**
     * 治疗玩家所有宠物
     */
    @PostMapping("/player/{playerId}/heal-all")
    public ApiResponse<String> healAllPets(@PathVariable Long playerId) {
        petManagementService.healAllPets(playerId);
        return ApiResponse.success("所有宠物已完全恢复", null);
    }
}
