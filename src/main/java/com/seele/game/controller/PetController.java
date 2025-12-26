package com.seele.game.controller;

import com.seele.game.dto.AddExpRequest;
import com.seele.game.dto.ApiResponse;
import com.seele.game.dto.LearnSkillRequest;
import com.seele.game.dto.PetCreateRequest;
import com.seele.game.dto.PetLevelSkillRequest;
import com.seele.game.dto.SkillCreateRequest;
import com.seele.game.entity.PetLevelSkill;
import com.seele.game.entity.PetTemplate;
import com.seele.game.entity.PlayerPet;
import com.seele.game.entity.PlayerPetSkill;
import com.seele.game.entity.Skill;
import com.seele.game.mapper.PetLevelSkillMapper;
import com.seele.game.mapper.SkillMapper;
import com.seele.game.service.IPetGrowthService;
import com.seele.game.service.IPetManagementService;
import com.seele.game.service.ISkillLearnService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
    private final PetLevelSkillMapper petLevelSkillMapper;
    private final SkillMapper skillMapper;

    /**
     * 获取所有初始宠物
     */
    @GetMapping("/starters")
    public ApiResponse<List<PetTemplate>> getStarterPets() {
        return ApiResponse.success(petManagementService.getStarterPets());
    }

    /**
     * 获取所有宠物模板
     */
    @GetMapping("/templates")
    public ApiResponse<List<PetTemplate>> getAllTemplates() {
        return ApiResponse.success(petManagementService.getAllPetTemplates());
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
     * 捕获/获取宠物（可以获取任意宠物）
     */
    @PostMapping("/catch")
    public ApiResponse<PlayerPet> catchPet(@Valid @RequestBody PetCreateRequest request) {
        try {
            // 使用默认等级5，如果请求中有等级则使用请求的等级
            int level = request.getLevel() != null ? request.getLevel() : 5;

            PlayerPet pet = petManagementService.createPet(
                    request.getPlayerId(),
                    request.getPetTemplateId(),
                    request.getNickname(),
                    level
            );
            return ApiResponse.success("成功捕获宠物！", pet);
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
        try {
            PlayerPet pet = petManagementService.getPetById(petId);
            return ApiResponse.success(pet);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(404, e.getMessage());
        }
    }

    /**
     * 增加经验值
     */
    @PostMapping("/add-exp")
    public ApiResponse<PlayerPet> addExp(@Valid @RequestBody AddExpRequest request) {
        try {
            // 直接根据宠物ID查询
            PlayerPet pet = petManagementService.getPetById(request.getPlayerPetId());

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

    /**
     * 获取宠物可学习的技能
     * 返回该宠物模板在当前等级及以下可以学习的所有技能
     */
    @GetMapping("/{petId}/available-skills")
    public ApiResponse<List<Skill>> getAvailableSkills(@PathVariable Long petId) {
        try {
            PlayerPet pet = petManagementService.getPetById(petId);

            // 获取该宠物模板在当前等级及以下可学习的技能
            List<PetLevelSkill> levelSkills = petLevelSkillMapper
                    .findByPetTemplateIdAndLearnLevelLessThanEqual(
                            pet.getPetTemplateId(),
                            pet.getLevel()
                    );

            // 获取技能详情
            List<Skill> skills = levelSkills.stream()
                    .map(ls -> skillMapper.selectById(ls.getSkillId()))
                    .collect(Collectors.toList());

            return ApiResponse.success(skills);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(404, e.getMessage());
        }
    }

    /**
     * 学习技能
     */
    @PostMapping("/learn-skill")
    public ApiResponse<PlayerPetSkill> learnSkill(@Valid @RequestBody LearnSkillRequest request) {
        try {
            PlayerPetSkill skill = skillLearnService.learnSkill(
                    request.getPlayerPetId(),
                    request.getSkillId(),
                    request.getReplacePosition()
            );
            return ApiResponse.success("技能学习成功", skill);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    /**
     * 获取所有技能（用于管理和测试）
     */
    @GetMapping("/skills/all")
    public ApiResponse<List<Skill>> getAllSkills() {
        try {
            List<Skill> skills = skillMapper.findAll();
            return ApiResponse.success(skills);
        } catch (Exception e) {
            return ApiResponse.error(500, "获取技能列表失败");
        }
    }

    /**
     * 创建新技能
     */
    @PostMapping("/skills/create")
    public ApiResponse<Skill> createSkill(@Valid @RequestBody SkillCreateRequest request) {
        try {
            Skill skill = new Skill();
            skill.setName(request.getName());
            skill.setSkillType(request.getSkillType());
            skill.setPetType(request.getPetType());
            skill.setPower(request.getPower());
            skill.setAccuracy(request.getAccuracy());
            skill.setMaxPp(request.getMaxPp());
            skill.setPriority(request.getPriority());
            skill.setStatusEffect(request.getStatusEffect());
            skill.setEffectChance(request.getEffectChance());
            skill.setDescription(request.getDescription());
            skill.setCreatedAt(LocalDateTime.now());
            skill.setUpdatedAt(LocalDateTime.now());

            skillMapper.insert(skill);
            return ApiResponse.success("技能创建成功", skill);
        } catch (Exception e) {
            return ApiResponse.error(500, "创建技能失败: " + e.getMessage());
        }
    }

    /**
     * 设置宠物模板在某等级学会某技能
     */
    @PostMapping("/skills/set-level-skill")
    public ApiResponse<PetLevelSkill> setPetLevelSkill(@Valid @RequestBody PetLevelSkillRequest request) {
        try {
            // 检查技能是否存在
            Skill skill = skillMapper.selectById(request.getSkillId());
            if (skill == null) {
                return ApiResponse.error(404, "技能不存在");
            }

            PetLevelSkill petLevelSkill = new PetLevelSkill();
            petLevelSkill.setPetTemplateId(request.getPetTemplateId());
            petLevelSkill.setSkillId(request.getSkillId());
            petLevelSkill.setLearnLevel(request.getLearnLevel());
            petLevelSkill.setCreatedAt(LocalDateTime.now());

            petLevelSkillMapper.insert(petLevelSkill);
            return ApiResponse.success("设置成功", petLevelSkill);
        } catch (Exception e) {
            return ApiResponse.error(500, "设置失败: " + e.getMessage());
        }
    }

    /**
     * 获取某个宠物模板的技能学习配置
     */
    @GetMapping("/templates/{templateId}/level-skills")
    public ApiResponse<List<PetLevelSkill>> getTemplateLevelSkills(@PathVariable Long templateId) {
        try {
            List<PetLevelSkill> levelSkills = petLevelSkillMapper.findByPetTemplateId(templateId);
            return ApiResponse.success(levelSkills);
        } catch (Exception e) {
            return ApiResponse.error(500, "获取技能配置失败: " + e.getMessage());
        }
    }
}
