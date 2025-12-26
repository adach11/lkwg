package com.seele.game.controller;

import com.seele.game.entity.PlayerTeam;
import com.seele.game.service.IPlayerTeamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 玩家队伍管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/team")
@RequiredArgsConstructor
public class PlayerTeamController {

    private final IPlayerTeamService playerTeamService;

    /**
     * 获取玩家的队伍
     */
    @GetMapping("/{playerId}")
    public Map<String, Object> getTeam(@PathVariable Long playerId) {
        List<PlayerTeam> team = playerTeamService.getPlayerTeam(playerId);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("team", team);
        response.put("size", team.stream().filter(t -> t.getPlayerPetId() != null).count());
        return response;
    }

    /**
     * 设置队伍中的宠物
     */
    @PostMapping("/{playerId}/set")
    public Map<String, Object> setTeamMember(
            @PathVariable Long playerId,
            @RequestParam Integer position,
            @RequestParam(required = false) Long petId) {

        PlayerTeam member = playerTeamService.setTeamMember(playerId, position, petId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "设置成功");
        response.put("teamMember", member);
        return response;
    }

    /**
     * 移除队伍中的宠物
     */
    @DeleteMapping("/{playerId}/remove/{position}")
    public Map<String, Object> removeTeamMember(
            @PathVariable Long playerId,
            @PathVariable Integer position) {

        playerTeamService.removeTeamMember(playerId, position);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "移除成功");
        return response;
    }

    /**
     * 批量设置队伍
     */
    @PostMapping("/{playerId}/batch-set")
    public Map<String, Object> setTeam(
            @PathVariable Long playerId,
            @RequestBody Long[] petIds) {

        playerTeamService.setTeam(playerId, petIds);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "队伍设置成功");
        response.put("teamSize", petIds.length);
        return response;
    }

    /**
     * 清空队伍
     */
    @DeleteMapping("/{playerId}/clear")
    public Map<String, Object> clearTeam(@PathVariable Long playerId) {
        playerTeamService.clearTeam(playerId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "队伍已清空");
        return response;
    }

    /**
     * 获取队伍中的宠物ID列表
     */
    @GetMapping("/{playerId}/pet-ids")
    public Map<String, Object> getTeamPetIds(@PathVariable Long playerId) {
        Long[] petIds = playerTeamService.getTeamPetIds(playerId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("petIds", petIds);
        response.put("count", petIds.length);
        return response;
    }
}
