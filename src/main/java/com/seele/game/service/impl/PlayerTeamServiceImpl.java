package com.seele.game.service.impl;

import com.seele.game.entity.PlayerPet;
import com.seele.game.entity.PlayerTeam;
import com.seele.game.mapper.PlayerPetMapper;
import com.seele.game.mapper.PlayerTeamMapper;
import com.seele.game.service.IPlayerTeamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 玩家队伍服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlayerTeamServiceImpl implements IPlayerTeamService {

    private final PlayerTeamMapper playerTeamMapper;
    private final PlayerPetMapper playerPetMapper;

    @Override
    public List<PlayerTeam> getPlayerTeam(Long playerId) {
        return playerTeamMapper.findByPlayerIdOrderByPosition(playerId);
    }

    @Override
    public List<PlayerTeam> getActiveTeam(Long playerId) {
        return playerTeamMapper.findByPlayerIdAndIsActiveTrue(playerId);
    }

    @Override
    @Transactional
    public PlayerTeam setTeamMember(Long playerId, Integer position, Long petId) {
        if (position < 1 || position > 6) {
            throw new RuntimeException("队伍位置必须在1-6之间");
        }

        // 验证宠物是否属于玩家
        if (petId != null) {
            PlayerPet pet = playerPetMapper.selectById(petId);
            if (pet == null) {
                throw new RuntimeException("宠物不存在: " + petId);
            }
            if (!pet.getPlayerId().equals(playerId)) {
                throw new RuntimeException("宠物不属于该玩家");
            }
        }

        // 查找该位置是否已有队伍成员
        PlayerTeam existing = playerTeamMapper.findByPlayerIdAndPosition(playerId, position);

        if (existing != null) {
            // 更新现有记录
            existing.setPlayerPetId(petId);
            existing.setUpdatedAt(LocalDateTime.now());
            playerTeamMapper.updateById(existing);
            log.info("更新队伍成员: playerId={}, position={}, petId={}", playerId, position, petId);
            return existing;
        } else {
            // 创建新记录
            PlayerTeam newMember = new PlayerTeam();
            newMember.setPlayerId(playerId);
            newMember.setPosition(position);
            newMember.setPlayerPetId(petId);
            newMember.setTeamName("默认队伍");
            newMember.setIsActive(true);
            newMember.setCreatedAt(LocalDateTime.now());
            newMember.setUpdatedAt(LocalDateTime.now());
            playerTeamMapper.insert(newMember);
            log.info("添加队伍成员: playerId={}, position={}, petId={}", playerId, position, petId);
            return newMember;
        }
    }

    @Override
    @Transactional
    public void removeTeamMember(Long playerId, Integer position) {
        if (position < 1 || position > 6) {
            throw new RuntimeException("队伍位置必须在1-6之间");
        }

        PlayerTeam existing = playerTeamMapper.findByPlayerIdAndPosition(playerId, position);
        if (existing != null) {
            existing.setPlayerPetId(null);
            existing.setUpdatedAt(LocalDateTime.now());
            playerTeamMapper.updateById(existing);
            log.info("移除队伍成员: playerId={}, position={}", playerId, position);
        }
    }

    @Override
    public Long[] getTeamPetIds(Long playerId) {
        List<PlayerTeam> team = getPlayerTeam(playerId);
        return team.stream()
                .filter(t -> t.getPlayerPetId() != null)
                .map(PlayerTeam::getPlayerPetId)
                .toArray(Long[]::new);
    }

    @Override
    @Transactional
    public void setTeam(Long playerId, Long[] petIds) {
        if (petIds == null || petIds.length > 6) {
            throw new RuntimeException("队伍宠物数量最多为6个");
        }

        // 清空现有队伍
        clearTeam(playerId);

        // 设置新队伍
        for (int i = 0; i < petIds.length; i++) {
            setTeamMember(playerId, i + 1, petIds[i]);
        }

        log.info("批量设置队伍: playerId={}, 队伍大小={}", playerId, petIds.length);
    }

    @Override
    @Transactional
    public void clearTeam(Long playerId) {
        playerTeamMapper.deleteByPlayerId(playerId);
        log.info("清空队伍: playerId={}", playerId);
    }
}
