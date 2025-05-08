package com.silverpotion.chatserver.notification.dto;

import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationMessageDto {
    private String loginId;
    private String title;
    private String content;
    private String type;
    private Long referenceId;
    private LocalDateTime createdAt;
}
