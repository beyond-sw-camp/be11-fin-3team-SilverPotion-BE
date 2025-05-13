package com.silverpotion.chatserver.notification.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.silverpotion.chatserver.chat.domain.MessageType;
import com.silverpotion.chatserver.chat.dto.ChatMessageDto;
import com.silverpotion.chatserver.chat.repository.ChatParticipantRepository;
import com.silverpotion.chatserver.notification.controller.SseController;
import com.silverpotion.chatserver.notification.domain.Notification;
import com.silverpotion.chatserver.notification.dto.NotificationCreateDto;
import com.silverpotion.chatserver.notification.dto.NotificationMessageDto;
import com.silverpotion.chatserver.notification.repository.NotificationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaSseService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final ChatParticipantRepository chatParticipantRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final SimpUserRegistry simpUserRegistry;
    private final SseController sseController;
    private final NotificationRepository notificationRepository;
    public void publishToSseTopic(ChatMessageDto dto) {
        log.info("🔥 발행 전 DTO: {}", dto);
        try {
            String message = objectMapper.writeValueAsString(dto);
            kafkaTemplate.send("chat-topic", message);
            log.info("📡 메시지 Kafka 발행됨: {}", message); // 발행된 메시지 확인
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
    @KafkaListener(
            topics = "chat-topic",
            groupId = "#{T(java.util.UUID).randomUUID().toString()}",
//            groupId = "#{@kafkaGroupId}",
            concurrency = "1" // ✅ 명시적으로 한 쓰레드만 사용하게 설정
    )
    public void consumeChatMessage(String messageJson) {
        log.warn("🔥 WebSocket Kafka Consumer 실행됨 @{}", System.identityHashCode(this));
        try {
            log.info("📡 수신된 메시지: {}", messageJson);
            ChatMessageDto message = objectMapper.readValue(messageJson, ChatMessageDto.class);

            List<String> loginIds = chatParticipantRepository.findLoginIdsByRoomId(message.getRoomId());
            log.info("🧩 연결된 유저 목록: {}", simpUserRegistry.getUsers().stream().map(SimpUser::getName).toList());
            log.info("📡 전송할 메시지 내용: {}", message);

            for (String loginId : loginIds) {
                boolean hasUser = simpUserRegistry.getUser(loginId) != null;
                log.info("🧩 대상 loginId = {}, SimpUserRegistry 등록 여부 = {}", loginId, hasUser);

                if (hasUser) {
                    messagingTemplate.convertAndSendToUser(loginId, "/chat", message);
                    log.info("📡 WebSocket 전송 → /user/{}/chat", loginId);
                } else {
                    sseController.sendToClientOrQueue(loginId, message);
                    log.info("📬 WebSocket 없음 → SSE 전송 시도(loginId: {})", loginId);
                }
            }
        } catch (Exception e) {
            log.error("❌ WebSocket Kafka Consumer 오류", e);
        }
    }


    @KafkaListener(
            topics = "notification-topic",
            groupId = "notification-group",
            concurrency = "1"
    )
    public void consumeNotification(String messageJson) {
        log.info("📨 알림 Kafka 수신됨: {}", messageJson);
        try {
            NotificationCreateDto dto = objectMapper.readValue(messageJson, NotificationCreateDto.class);
            LocalDateTime now = LocalDateTime.now();
            // 🔸 DB에 저장
            Notification notification = Notification.builder()
                    .loginId(dto.getLoginId())
                    .title(dto.getTitle())
                    .content(dto.getContent())
                    .type(dto.getType())
                    .referenceId(dto.getReferenceId())
                    .createdAt(LocalDateTime.now())
                    .isRead(false)
                    .build();
            notificationRepository.save(notification);

            // 🔸 SSE 전송
            NotificationMessageDto message = NotificationMessageDto.builder()
                    .loginId(dto.getLoginId())
                    .title(dto.getTitle())
                    .content(dto.getContent())
                    .type(dto.getType())
                    .referenceId(dto.getReferenceId())
                    .createdAt(now)
                    .build();

            sseController.sendToClientOrQueue(dto.getLoginId(), message);
            log.info("📡 알림 전송 완료 → {}", dto.getLoginId());

        } catch (Exception e) {
            log.error("❌ 알림 처리 실패", e);
        }
    }
    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.markAsReadById(notificationId);
    }
}