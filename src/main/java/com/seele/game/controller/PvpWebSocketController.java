package com.seele.game.controller;

import com.seele.game.battle.BattleRoundResult;
import com.seele.game.battle.BattleState;
import com.seele.game.pvp.PvpRoom;
import com.seele.game.pvp.PvpRoomManager;
import com.seele.game.service.IBattleService;
import com.seele.game.service.IPetManagementService;
import com.seele.game.websocket.event.*;
import com.seele.game.websocket.message.*;
import com.seele.game.entity.PlayerPet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

/**
 * PVP WebSocket控制器
 * 处理所有PVP实时对战的WebSocket消息
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class PvpWebSocketController {

    private final PvpRoomManager roomManager;
    private final IBattleService battleService;
    private final IPetManagementService petManagementService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 创建房间
     */
    @MessageMapping("/pvp/createRoom")
    public void createRoom(@Payload CreateRoomMessage message) {
        try {
            log.info("收到创建房间请求: playerId={}, petId={}", message.getPlayerId(), message.getPetId());

            // 获取宠物
            PlayerPet pet = petManagementService.getPetById(message.getPetId());
            if (pet == null) {
                sendError(message.getPlayerId(), "宠物不存在");
                return;
            }

            if (!pet.getPlayerId().equals(message.getPlayerId())) {
                sendError(message.getPlayerId(), "宠物不属于你");
                return;
            }

            // 创建房间
            PvpRoom room = roomManager.createRoom(message.getPlayerId(), pet);

            // 发送房间创建成功事件
            RoomCreatedEvent event = RoomCreatedEvent.builder()
                    .roomId(room.getRoomId())
                    .creatorId(room.getPlayer1Id())
                    .creatorName("玩家" + room.getPlayer1Id()) // 可以从用户服务获取昵称
                    .petName(pet.getNickname())
                    .petLevel(pet.getLevel())
                    .message("房间创建成功！等待对手加入...")
                    .build();

            sendToUser(message.getPlayerId(), "/queue/room", event);

        } catch (Exception e) {
            log.error("创建房间失败", e);
            sendError(message.getPlayerId(), "创建房间失败: " + e.getMessage());
        }
    }

    /**
     * 加入房间
     */
    @MessageMapping("/pvp/joinRoom")
    public void joinRoom(@Payload JoinRoomMessage message) {
        try {
            log.info("收到加入房间请求: playerId={}, roomId={}, petId={}",
                    message.getPlayerId(), message.getRoomId(), message.getPetId());

            // 获取宠物
            PlayerPet pet = petManagementService.getPetById(message.getPetId());
            if (pet == null) {
                sendError(message.getPlayerId(), "宠物不存在");
                return;
            }

            if (!pet.getPlayerId().equals(message.getPlayerId())) {
                sendError(message.getPlayerId(), "宠物不属于你");
                return;
            }

            // 加入房间
            PvpRoom room = roomManager.joinRoom(message.getRoomId(), message.getPlayerId(), pet);

            // 通知玩家2加入成功
            OpponentJoinedEvent player2Event = OpponentJoinedEvent.builder()
                    .opponentId(room.getPlayer1Id())
                    .opponentName("玩家" + room.getPlayer1Id())
                    .petName(room.getPlayer1Pet().getNickname())
                    .petLevel(room.getPlayer1Pet().getLevel())
                    .message("成功加入房间！")
                    .build();
            sendToUser(message.getPlayerId(), "/queue/opponent", player2Event);

            // 通知玩家1对手已加入
            OpponentJoinedEvent player1Event = OpponentJoinedEvent.builder()
                    .opponentId(room.getPlayer2Id())
                    .opponentName("玩家" + room.getPlayer2Id())
                    .petName(room.getPlayer2Pet().getNickname())
                    .petLevel(room.getPlayer2Pet().getLevel())
                    .message("对手已加入！")
                    .build();
            sendToUser(room.getPlayer1Id(), "/queue/opponent", player1Event);

            // 创建战斗
            startBattle(room);

        } catch (Exception e) {
            log.error("加入房间失败", e);
            sendError(message.getPlayerId(), "加入房间失败: " + e.getMessage());
        }
    }

    /**
     * 随机匹配
     */
    @MessageMapping("/pvp/matchmaking")
    public void matchmaking(@Payload MatchmakingMessage message) {
        try {
            log.info("收到随机匹配请求: playerId={}, petId={}", message.getPlayerId(), message.getPetId());

            // 获取宠物
            PlayerPet pet = petManagementService.getPetById(message.getPetId());
            if (pet == null) {
                sendError(message.getPlayerId(), "宠物不存在");
                return;
            }

            if (!pet.getPlayerId().equals(message.getPlayerId())) {
                sendError(message.getPlayerId(), "宠物不属于你");
                return;
            }

            // 随机匹配
            PvpRoom room = roomManager.matchmaking(message.getPlayerId(), pet);

            if (room.isFull()) {
                // 匹配成功，立即开始战斗
                log.info("匹配成功: roomId={}", room.getRoomId());

                // 通知双方匹配成功
                OpponentJoinedEvent player1Event = OpponentJoinedEvent.builder()
                        .opponentId(room.getPlayer2Id())
                        .opponentName("玩家" + room.getPlayer2Id())
                        .petName(room.getPlayer2Pet().getNickname())
                        .petLevel(room.getPlayer2Pet().getLevel())
                        .message("匹配成功！")
                        .build();
                sendToUser(room.getPlayer1Id(), "/queue/opponent", player1Event);

                OpponentJoinedEvent player2Event = OpponentJoinedEvent.builder()
                        .opponentId(room.getPlayer1Id())
                        .opponentName("玩家" + room.getPlayer1Id())
                        .petName(room.getPlayer1Pet().getNickname())
                        .petLevel(room.getPlayer1Pet().getLevel())
                        .message("匹配成功！")
                        .build();
                sendToUser(room.getPlayer2Id(), "/queue/opponent", player2Event);

                // 创建战斗
                startBattle(room);

            } else {
                // 等待匹配
                RoomCreatedEvent event = RoomCreatedEvent.builder()
                        .roomId(room.getRoomId())
                        .creatorId(room.getPlayer1Id())
                        .creatorName("玩家" + room.getPlayer1Id())
                        .petName(pet.getNickname())
                        .petLevel(pet.getLevel())
                        .message("正在匹配对手...")
                        .build();
                sendToUser(message.getPlayerId(), "/queue/room", event);
            }

        } catch (Exception e) {
            log.error("随机匹配失败", e);
            sendError(message.getPlayerId(), "随机匹配失败: " + e.getMessage());
        }
    }

    /**
     * 提交技能选择
     */
    @MessageMapping("/pvp/selectSkill")
    public void selectSkill(@Payload SkillSelectMessage message) {
        try {
            log.info("收到技能选择: battleId={}, playerId={}, skillId={}",
                    message.getBattleId(), message.getPlayerId(), message.getSkillId());

            // 提交技能选择
            boolean bothReady = battleService.submitSkillChoice(
                    message.getBattleId(),
                    message.getPlayerId(),
                    message.getSkillId()
            );

            BattleState battle = battleService.getBattleState(message.getBattleId());

            // 获取技能名称
            String skillName = getSkillName(battle, message.getPlayerId(), message.getSkillId());

            // 通知玩家已选择
            WaitingForOpponentEvent waitEvent = WaitingForOpponentEvent.builder()
                    .battleId(message.getBattleId())
                    .yourSkillName(skillName)
                    .message("已选择技能: " + skillName + "，等待对手...")
                    .build();
            sendToUser(message.getPlayerId(), "/queue/battle", waitEvent);

            // 如果双方都准备好，执行回合
            if (bothReady) {
                executeBattleRound(battle);
            }

        } catch (Exception e) {
            log.error("提交技能选择失败", e);
            sendError(message.getPlayerId(), "提交技能失败: " + e.getMessage());
        }
    }

    /**
     * 开始战斗
     */
    private void startBattle(PvpRoom room) {
        try {
            // 创建战斗
            BattleState battle = battleService.createPvpBattle(
                    room.getPlayer1Id(),
                    room.getPlayer1Pet().getId(),
                    room.getPlayer2Id(),
                    room.getPlayer2Pet().getId()
            );

            // 更新房间状态
            roomManager.startBattle(room.getRoomId(), battle.getBattleId());

            // 发送战斗开始事件给玩家1
            BattleStartEvent.BattleStateSnapshot snapshot1 = buildBattleSnapshot(battle);
            BattleStartEvent startEvent1 = BattleStartEvent.builder()
                    .battleId(battle.getBattleId())
                    .yourPlayerNumber(1)
                    .battleState(snapshot1)
                    .message("战斗开始！请选择技能")
                    .build();
            sendToUser(room.getPlayer1Id(), "/queue/battle", startEvent1);

            // 发送战斗开始事件给玩家2
            BattleStartEvent.BattleStateSnapshot snapshot2 = buildBattleSnapshot(battle);
            BattleStartEvent startEvent2 = BattleStartEvent.builder()
                    .battleId(battle.getBattleId())
                    .yourPlayerNumber(2)
                    .battleState(snapshot2)
                    .message("战斗开始！请选择技能")
                    .build();
            sendToUser(room.getPlayer2Id(), "/queue/battle", startEvent2);

            log.info("战斗开始通知已发送: battleId={}", battle.getBattleId());

        } catch (Exception e) {
            log.error("开始战斗失败", e);
            sendError(room.getPlayer1Id(), "开始战斗失败: " + e.getMessage());
            sendError(room.getPlayer2Id(), "开始战斗失败: " + e.getMessage());
        }
    }

    /**
     * 执行战斗回合
     */
    private void executeBattleRound(BattleState battle) {
        try {
            // 执行回合
            BattleRoundResult result = battleService.executePvpRound(battle.getBattleId());

            // 发送回合结果给双方
            RoundResultEvent resultEvent = RoundResultEvent.builder()
                    .battleId(battle.getBattleId())
                    .roundResult(result)
                    .nextTurnTimeout(30)
                    .message("回合" + result.getTurn() + "结束")
                    .build();

            sendToUser(battle.getPlayer1Id(), "/queue/battle", resultEvent);
            sendToUser(battle.getPlayer2Id(), "/queue/battle", resultEvent);

            // 如果战斗结束，发送战斗结束事件
            if (result.isBattleEnded()) {
                sendBattleEndEvents(battle);
            }

            log.info("回合结果已发送: battleId={}, turn={}", battle.getBattleId(), result.getTurn());

        } catch (Exception e) {
            log.error("执行战斗回合失败", e);
            sendError(battle.getPlayer1Id(), "执行回合失败: " + e.getMessage());
            sendError(battle.getPlayer2Id(), "执行回合失败: " + e.getMessage());
        }
    }

    /**
     * 发送战斗结束事件
     */
    private void sendBattleEndEvents(BattleState battle) {
        boolean player1Won = "玩家1".equals(battle.getWinner());
        boolean player2Won = "玩家2".equals(battle.getWinner());

        // 发送给玩家1
        BattleEndEvent endEvent1 = BattleEndEvent.builder()
                .battleId(battle.getBattleId())
                .winner(battle.getWinner())
                .youWon(player1Won)
                .expGained(player1Won ? 100 : 50) // 简化处理
                .leveledUp(false)
                .message(player1Won ? "你赢了！" : (player2Won ? "你输了" : "平局"))
                .build();
        sendToUser(battle.getPlayer1Id(), "/queue/battle", endEvent1);

        // 发送给玩家2
        BattleEndEvent endEvent2 = BattleEndEvent.builder()
                .battleId(battle.getBattleId())
                .winner(battle.getWinner())
                .youWon(player2Won)
                .expGained(player2Won ? 100 : 50) // 简化处理
                .leveledUp(false)
                .message(player2Won ? "你赢了！" : (player1Won ? "你输了" : "平局"))
                .build();
        sendToUser(battle.getPlayer2Id(), "/queue/battle", endEvent2);

        log.info("战斗结束事件已发送: battleId={}, winner={}", battle.getBattleId(), battle.getWinner());
    }

    /**
     * 构建战斗状态快照
     */
    private BattleStartEvent.BattleStateSnapshot buildBattleSnapshot(BattleState battle) {
        BattleStartEvent.PetSnapshot player1Pet = BattleStartEvent.PetSnapshot.builder()
                .name(battle.getPlayer1Pet().getOriginalPet().getNickname())
                .level(battle.getPlayer1Pet().getOriginalPet().getLevel())
                .type(battle.getPlayer1Pet().getOriginalPet().getTemplate().getType().getDisplayName())
                .currentHp(battle.getPlayer1Pet().getCurrentHp())
                .maxHp(battle.getPlayer1Pet().getMaxHp())
                .status(battle.getPlayer1Pet().getStatusCondition().name())
                .build();

        BattleStartEvent.PetSnapshot player2Pet = BattleStartEvent.PetSnapshot.builder()
                .name(battle.getPlayer2Pet().getOriginalPet().getNickname())
                .level(battle.getPlayer2Pet().getOriginalPet().getLevel())
                .type(battle.getPlayer2Pet().getOriginalPet().getTemplate().getType().getDisplayName())
                .currentHp(battle.getPlayer2Pet().getCurrentHp())
                .maxHp(battle.getPlayer2Pet().getMaxHp())
                .status(battle.getPlayer2Pet().getStatusCondition().name())
                .build();

        return BattleStartEvent.BattleStateSnapshot.builder()
                .battleId(battle.getBattleId())
                .currentTurn(battle.getCurrentTurn())
                .player1Pet(player1Pet)
                .player2Pet(player2Pet)
                .build();
    }

    /**
     * 获取技能名称
     */
    private String getSkillName(BattleState battle, Long playerId, Long skillId) {
        boolean isPlayer1 = battle.getPlayer1Id().equals(playerId);
        var pet = isPlayer1 ? battle.getPlayer1Pet() : battle.getPlayer2Pet();

        return pet.getOriginalPet().getSkills().stream()
                .filter(s -> s.getSkill().getId().equals(skillId))
                .map(s -> s.getSkill().getName())
                .findFirst()
                .orElse("未知技能");
    }

    /**
     * 发送消息给指定用户（使用topic广播）
     */
    private void sendToUser(Long userId, String destination, Object payload) {
        // 使用topic方式广播到特定玩家的频道
        // 格式: /topic/player/{playerId}/{destination}
        String topicDestination = "/topic/player/" + userId + destination;
        messagingTemplate.convertAndSend(topicDestination, payload);
    }

    /**
     * 发送错误消息
     */
    private void sendError(Long userId, String errorMessage) {
        sendToUser(userId, "/queue/error", errorMessage);
    }
}
