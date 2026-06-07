package com.example.gym.dto;

import lombok.Data;

public class CommentDTO {

    @Data
    public static class AddReq {
        private Long courseId;
        private String content;
        /** 回复某条评论时传入，顶级评论时为 null */
        private Long parentId;
    }
}
