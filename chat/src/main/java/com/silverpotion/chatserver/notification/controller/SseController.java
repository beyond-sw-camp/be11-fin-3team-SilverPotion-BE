package com.silverpotion.chatserver.notification.controller;

import com.silverpotion.chatserver.chat.domain.ChatParticipant;
import com.silverpotion.chatserver.chat.dto.ChatMessageDto;
import com.silverpotion.chatserver.chat.repository.ChatParticipantRepository;
import com.silverpotion.chatserver.chat.service.UserFeign;
import com.silverpotion.chatserver.common.annotation.LoginUser;
import com.silverpotion.chatserver.notification.dto.NotificationMessageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/sse")
@RequiredArgsConstructor
public class SseController {

    private final Map<String, SseEmitter> emitterMap = new ConcurrentHashMap<>();
    private final ChatParticipantRepository chatParticipantRepository;
    private final UserFeign userFeign;

    @CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
    @GetMapping("/subscribe")
    public SseEmitter subscribe(@RequestParam String loginId) {
        System.out.println("subscribe loginUser: " + loginId);
        if (emitterMap.containsKey(loginId)) {
            SseEmitter oldEmitter = emitterMap.get(loginId);
            if (oldEmitter != null) oldEmitter.complete();
            emitterMap.remove(loginId);
        }

        SseEmitter emitter = new SseEmitter(2 * 60 * 1000L); // 2분 타임아웃
        emitterMap.put(loginId, emitter);

        try {
            emitter.send(SseEmitter.event().name("connect").data("연결완료"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return emitter;
    }

    @GetMapping("/unsubscribe")
    public void unsubscribe( String loginUser) {
        emitterMap.remove(loginUser);
    }

    public void publishMessage(ChatMessageDto chatMessageDto, Long chatRoomId) {
        List<ChatParticipant> offlineUsers = chatParticipantRepository.findByChatRoomIdAndIsConnectedFalse(chatRoomId);

        for (ChatParticipant participant : offlineUsers) {

            String loginId = userFeign.getLoginIdByUserId(participant.getUserId());
            SseEmitter emitter = emitterMap.get(loginId);

            if (emitter != null) {
                try {
                    emitter.send(SseEmitter.event().name("chat-message").data(chatMessageDto));
                } catch (IOException e) {
                    emitterMap.remove(loginId);
                }
            }
        }
    }
    public void sendToClientOrQueue(String loginId, NotificationMessageDto message) {
        SseEmitter emitter = emitterMap.get(loginId);

        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().name("notification").data(message));
            } catch (IOException e) {
                emitterMap.remove(loginId);
            }
        } else {
            // 💡 현재는 SSE에 연결 안 된 경우 무시
            // 필요 시, DB나 Redis에 저장하여 미수신 알림으로 처리 가능
            System.out.println("❌ SSE 미연결 상태 - loginId: " + loginId + ", 알림 저장 또는 무시");
        }
    }
}
