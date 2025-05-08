package com.silverpotion.chatserver.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationCreateDto {
    private String loginId;     // 받을 사람
    private String title;       // 알림 제목
    private String content;     // 알림 내용
    private String type;        // 알림 타입 (join_request 등)
    private Long referenceId;
    private LocalDateTime createdAt;
}