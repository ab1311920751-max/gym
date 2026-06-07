package com.example.gym.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CourseCommentVO {

    private Long id;

    private Long courseId;

    private Long userId;

    private String username;

    private String nickname;

    private String content;

    private Long parentId;

    /** 被回复用户的用户名，用于前端展示 "@xxx" */
    private String parentUsername;

    private LocalDateTime createTime;

    /** 子评论列表，仅顶级评论持有 */
    private List<CourseCommentVO> replies;
}
