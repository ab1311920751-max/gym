package com.example.gym.service;

import com.example.gym.dto.CommentDTO;
import com.example.gym.vo.CourseCommentVO;

import java.util.List;

public interface CommentService {

    List<CourseCommentVO> listByCourse(Long courseId);

    void addComment(Long uid, CommentDTO.AddReq req);

    void deleteComment(Long uid, String role, Long id);
}
