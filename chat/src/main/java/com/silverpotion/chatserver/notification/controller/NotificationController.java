package com.silverpotion.chatserver.notification.controller;

import com.silverpotion.chatserver.chat.domain.MessageType;
import com.silverpotion.chatserver.chat.dto.ChatMessageDto;
import com.silverpotion.chatserver.notification.domain.Notification;
import com.silverpotion.chatserver.notification.dto.NotificationCreateDto;
import com.silverpotion.chatserver.notification.dto.NotificationMessageDto;
import com.silverpotion.chatserver.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final SseController sseController;
    private final NotificationRepository notificationRepository;

    @PostMapping("/send")
    public ResponseEntity<Void> send(@RequestBody NotificationCreateDto dto) {
        NotificationMessageDto message = NotificationMessageDto.builder()
                .title(dto.getTitle())
                .type(dto.getType())
                .referenceId(dto.getReferenceId())
                .createdAt(dto.getCreatedAt())
                .content(dto.getContent())
                .loginId(dto.getLoginId())
                .build();

        sseController.sendToClientOrQueue(dto.getLoginId(), message);
        return ResponseEntity.ok().build();
    }
    @GetMapping("/list")
    public List<Notification> getMyNotifications(@RequestHeader("X-User-LoginId") String loginId) {
        return notificationRepository.findByLoginIdOrderByCreatedAtDesc(loginId);
    }
}
