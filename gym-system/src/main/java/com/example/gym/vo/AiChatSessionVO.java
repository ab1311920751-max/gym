package com.example.gym.vo;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class AiChatSessionVO {
    private Long id;
    private String title;
    private String lastMessage;
    private LocalDateTime updateTime;
}
