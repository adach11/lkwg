package com.seele.game.controller;

import com.seele.game.battle.BattlePetState;
import com.seele.game.battle.BattleRoundResult;
import com.seele.game.battle.BattleState;
import com.seele.game.dto.*;
import com.seele.game.entity.PlayerPetSkill;
import com.seele.game.service.IBattleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 战斗控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/battle")
@RequiredArgsConstructor
public class BattleController {

    private final IBattleService battleService;

    /**
     * 创建PVE战斗
     */
    @PostMapping("/pve/start")
    public ApiResponse<BattleStateResponse> startPveBattle(@Valid @RequestBody BattleStartRequest request) {
        try {
            BattleState battle = battleService.createPveBattle(
                    request.getPlayerId(),
                    request.getPetId(),
                    request.getDifficulty()
            );

            BattleStateResponse response = buildBattleStateResponse(battle);
            return ApiResponse.success("战斗创建成功！", response);
        } catch (Exception e) {
            log.error("创建战斗失败", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取战斗状态
     */
    @GetMapping("/{battleId}")
    public ApiResponse<BattleStateResponse> getBattleState(@PathVariable String battleId) {
        try {
            BattleState battle = battleService.getBattleState(battleId);
            BattleStateResponse response = buildBattleStateResponse(battle);
            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("获取战斗状态失败", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 执行攻击
     */
    @PostMapping("/{battleId}/attack")
    public ApiResponse<BattleRoundResult> attack(
            @PathVariable String battleId,
            @RequestParam Long playerId,
            @Valid @RequestBody BattleAttackRequest request) {
        try {
            BattleRoundResult result = battleService.executeRound(battleId, playerId, request.getSkillId());
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("执行攻击失败", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 投降
     */
    @PostMapping("/{battleId}/surrender")
    public ApiResponse<BattleEndResponse> surrender(@PathVariable String battleId) {
        try {
            BattleEndResponse response = battleService.endBattle(battleId);
            return ApiResponse.success("你投降了", response);
        } catch (Exception e) {
            log.error("投降失败", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 创建队伍PVE战斗
     */
    @PostMapping("/pve/team/start")
    public ApiResponse<BattleStateResponse> startTeamPveBattle(
            @RequestParam Long playerId,
            @RequestBody Long[] petIds,
            @RequestParam String difficulty) {
        try {
            BattleState battle = battleService.createTeamPveBattle(
                    playerId,
                    petIds,
                    com.seele.game.enums.AIDifficulty.valueOf(difficulty.toUpperCase())
            );

            BattleStateResponse response = buildTeamBattleStateResponse(battle);
            return ApiResponse.success("队伍战斗创建成功！", response);
        } catch (Exception e) {
            log.error("创建队伍战斗失败", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 切换宠物
     */
    @PostMapping("/{battleId}/switch")
    public ApiResponse<String> switchPet(
            @PathVariable String battleId,
            @RequestParam Long playerId,
            @RequestParam int switchToIndex) {
        try {
            boolean ready = battleService.submitSwitchPet(battleId, playerId, switchToIndex);
            if (ready) {
                return ApiResponse.success("切换宠物成功，双方已准备好！");
            } else {
                return ApiResponse.success("切换宠物成功，等待对手选择...");
            }
        } catch (Exception e) {
            log.error("切换宠物失败", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 构建战斗状态响应（单宠物模式）
     */
    private BattleStateResponse buildBattleStateResponse(BattleState battle) {
        return BattleStateResponse.builder()
                .battleId(battle.getBattleId())
                .status(battle.getStatus())
                .currentTurn(battle.getCurrentTurn())
                .playerPet(buildPetInfo(battle.getPlayerPet()))
                .aiPet(buildPetInfo(battle.getAiPet()))
                .aiTrainerName(battle.getAiTrainer() != null ? battle.getAiTrainer().getName() : null)
                .availableSkills(buildSkillInfoList(battle.getPlayerPet()))
                .build();
    }

    /**
     * 构建战斗状态响应（队伍模式）
     */
    private BattleStateResponse buildTeamBattleStateResponse(BattleState battle) {
        if (!battle.isTeamMode()) {
            return buildBattleStateResponse(battle);
        }

        return BattleStateResponse.builder()
                .battleId(battle.getBattleId())
                .status(battle.getStatus())
                .currentTurn(battle.getCurrentTurn())
                .playerPet(buildPetInfo(battle.getPlayer1ActivePet()))
                .aiPet(buildPetInfo(battle.getPlayer2ActivePet()))
                .aiTrainerName(battle.getAiTrainer() != null ? battle.getAiTrainer().getName() : "对手")
                .availableSkills(buildSkillInfoList(battle.getPlayer1ActivePet()))
                .build();
    }

    /**
     * 构建宠物信息
     */
    private BattleStateResponse.PetBattleInfo buildPetInfo(BattlePetState pet) {
        return BattleStateResponse.PetBattleInfo.builder()
                .id(pet.getOriginalPet().getId())
                .name(pet.getOriginalPet().getNickname())
                .level(pet.getOriginalPet().getLevel())
                .type(pet.getOriginalPet().getTemplate().getType().getDisplayName())
                .currentHp(pet.getCurrentHp())
                .maxHp(pet.getMaxHp())
                .status(pet.getStatusCondition().name())
                .fainted(pet.isFainted())
                .build();
    }

    /**
     * 构建技能信息列表
     */
    private List<BattleStateResponse.SkillInfo> buildSkillInfoList(BattlePetState pet) {
        return pet.getOriginalPet().getSkills().stream()
                .map(skill -> BattleStateResponse.SkillInfo.builder()
                        .id(skill.getSkill().getId())
                        .name(skill.getSkill().getName())
                        .type(skill.getSkill().getPetType() != null ?
                                skill.getSkill().getPetType().getDisplayName() : "通用")
                        .power(skill.getSkill().getPower())
                        .accuracy(skill.getSkill().getAccuracy())
                        .currentPp(pet.getSkillPpMap().getOrDefault(skill.getSkill().getId(), 0))
                        .maxPp(skill.getSkill().getMaxPp())
                        .build())
                .collect(Collectors.toList());
    }
}
