package com.silverpotion.chatserver.chat.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.silverpotion.chatserver.chat.dto.ChatMessageDto;
import com.silverpotion.chatserver.chat.service.ChatMessageService;
import com.silverpotion.chatserver.notification.service.KafkaSseService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class StompController {

    private final SimpMessageSendingOperations messagingTemplate;
    private final ChatMessageService chatMessageService;
    private final ObjectMapper objectMapper;
    private final KafkaSseService kafkaSseService;

    @MessageMapping("/room/{roomId}")
    public void sendMessage(@DestinationVariable Long roomId, Message<?> message) {
        // 1. STOMP 세션에서 loginId 꺼냄
        System.out.println("✅ [StompController] sendMessage() 호출됨");
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        Long userId = (Long) accessor.getSessionAttributes().get("id");
        if (userId == null) {
            System.out.println("❌ loginId 세션 없음");
            return;
        }

        // 2. payload 직접 파싱
        ChatMessageDto dto = parseMessage(message);
        dto.setSenderId(userId);
        dto.setCreatedAt(LocalDateTime.now());
        System.out.println("message : "+message.getPayload());
        // 3. 저장
        ChatMessageDto saved = chatMessageService.saveAndPublish(roomId, dto);
        // 4. 브로드캐스트
        messagingTemplate.convertAndSend("/sub/room/" + roomId, saved);
    }

    //메시지 파싱해주는 서브 메서드
    private ChatMessageDto parseMessage(Message<?> message) {
        try {
            String payload;

            if (message.getPayload() instanceof byte[]) {
                payload = new String((byte[]) message.getPayload(), StandardCharsets.UTF_8);
            } else {
                payload = message.getPayload().toString();
            }

            System.out.println("📨 수신된 raw payload = " + payload);

            return objectMapper.readValue(payload, ChatMessageDto.class);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("❌ 메시지 파싱 실패", e);
        }
    }
}
