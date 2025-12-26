package com.seele.game.pvp;

import com.seele.game.entity.PlayerPet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * PVP房间管理器
 * 管理所有PVP房间和匹配队列
 */
@Slf4j
@Component
public class PvpRoomManager {

    // 所有房间（roomId -> PvpRoom）
    private final Map<String, PvpRoom> rooms = new ConcurrentHashMap<>();

    // 匹配队列（等待匹配的房间）
    private final ConcurrentLinkedQueue<String> matchmakingQueue = new ConcurrentLinkedQueue<>();

    // 玩家所在房间映射（playerId -> roomId）
    private final Map<Long, String> playerRoomMap = new ConcurrentHashMap<>();

    /**
     * 创建房间
     */
    public PvpRoom createRoom(Long playerId, PlayerPet pet) {
        // 检查玩家是否已在其他房间
        String existingRoomId = playerRoomMap.get(playerId);
        if (existingRoomId != null) {
            PvpRoom existingRoom = rooms.get(existingRoomId);
            if (existingRoom != null && existingRoom.getStatus() != PvpRoom.RoomStatus.FINISHED) {
                throw new RuntimeException("你已经在房间中: " + existingRoomId);
            }
        }

        String roomId = UUID.randomUUID().toString();
        PvpRoom room = new PvpRoom(roomId, playerId, pet);
        rooms.put(roomId, room);
        playerRoomMap.put(playerId, roomId);

        log.info("房间创建成功: roomId={}, player1Id={}", roomId, playerId);
        return room;
    }

    /**
     * 加入房间
     */
    public PvpRoom joinRoom(String roomId, Long playerId, PlayerPet pet) {
        PvpRoom room = rooms.get(roomId);
        if (room == null) {
            throw new RuntimeException("房间不存在: " + roomId);
        }

        if (room.getStatus() != PvpRoom.RoomStatus.WAITING) {
            throw new RuntimeException("房间已满或已开始战斗");
        }

        if (room.getPlayer1Id().equals(playerId)) {
            throw new RuntimeException("不能加入自己创建的房间");
        }

        // 检查玩家是否已在其他房间
        String existingRoomId = playerRoomMap.get(playerId);
        if (existingRoomId != null && !existingRoomId.equals(roomId)) {
            PvpRoom existingRoom = rooms.get(existingRoomId);
            if (existingRoom != null && existingRoom.getStatus() != PvpRoom.RoomStatus.FINISHED) {
                throw new RuntimeException("你已经在其他房间中");
            }
        }

        room.joinPlayer2(playerId, pet);
        playerRoomMap.put(playerId, roomId);

        // 从匹配队列移除（如果在队列中）
        matchmakingQueue.remove(roomId);

        log.info("玩家加入房间: roomId={}, player2Id={}", roomId, playerId);
        return room;
    }

    /**
     * 随机匹配
     * 创建房间并加入匹配队列，或者匹配到等待中的房间
     */
    public PvpRoom matchmaking(Long playerId, PlayerPet pet) {
        // 尝试从队列中获取等待的房间
        String waitingRoomId = matchmakingQueue.poll();

        if (waitingRoomId != null) {
            PvpRoom waitingRoom = rooms.get(waitingRoomId);
            if (waitingRoom != null && waitingRoom.getStatus() == PvpRoom.RoomStatus.WAITING) {
                // 加入等待的房间
                try {
                    return joinRoom(waitingRoomId, playerId, pet);
                } catch (Exception e) {
                    // 如果加入失败，继续创建新房间
                    log.warn("加入等待房间失败: {}", e.getMessage());
                }
            }
        }

        // 没有等待的房间，创建新房间并加入队列
        PvpRoom newRoom = createRoom(playerId, pet);
        matchmakingQueue.offer(newRoom.getRoomId());
        log.info("创建新房间并加入匹配队列: roomId={}", newRoom.getRoomId());
        return newRoom;
    }

    /**
     * 获取房间
     */
    public PvpRoom getRoom(String roomId) {
        PvpRoom room = rooms.get(roomId);
        if (room == null) {
            throw new RuntimeException("房间不存在: " + roomId);
        }
        return room;
    }

    /**
     * 获取玩家所在房间
     */
    public PvpRoom getPlayerRoom(Long playerId) {
        String roomId = playerRoomMap.get(playerId);
        if (roomId == null) {
            return null;
        }
        return rooms.get(roomId);
    }

    /**
     * 房间开始战斗
     */
    public void startBattle(String roomId, String battleId) {
        PvpRoom room = getRoom(roomId);
        room.startBattle(battleId);
        log.info("房间开始战斗: roomId={}, battleId={}", roomId, battleId);
    }

    /**
     * 房间结束战斗
     */
    public void finishBattle(String roomId) {
        PvpRoom room = getRoom(roomId);
        room.finishBattle();
        log.info("房间结束战斗: roomId={}", roomId);
    }

    /**
     * 移除房间
     */
    public void removeRoom(String roomId) {
        PvpRoom room = rooms.remove(roomId);
        if (room != null) {
            playerRoomMap.remove(room.getPlayer1Id());
            if (room.getPlayer2Id() != null) {
                playerRoomMap.remove(room.getPlayer2Id());
            }
            matchmakingQueue.remove(roomId);
            log.info("移除房间: roomId={}", roomId);
        }
    }

    /**
     * 定时清理超时房间（每5分钟执行一次）
     */
    @Scheduled(fixedRate = 300000)
    public void cleanupTimeoutRooms() {
        int removed = 0;
        for (var entry : rooms.entrySet()) {
            PvpRoom room = entry.getValue();
            if (room.isTimeout() ||
                (room.getStatus() == PvpRoom.RoomStatus.FINISHED &&
                 room.getCreateTime().plusMinutes(30).isBefore(java.time.LocalDateTime.now()))) {
                removeRoom(entry.getKey());
                removed++;
            }
        }
        if (removed > 0) {
            log.info("清理了{}个超时/结束的PVP房间", removed);
        }
    }
}
