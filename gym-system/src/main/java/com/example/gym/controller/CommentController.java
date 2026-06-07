package com.example.gym.controller;

import com.example.gym.common.Result;
import com.example.gym.common.auth.CurrentUserId;
import com.example.gym.common.auth.CurrentUserIdResolver;
import com.example.gym.dto.CommentDTO;
import com.example.gym.service.CommentService;
import com.example.gym.vo.CourseCommentVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /** 获取课程评论列表，未登录也可查看（已在白名单中排除鉴权） */
    @GetMapping("/course/{courseId}")
    public Result<List<CourseCommentVO>> listByCourse(@PathVariable Long courseId) {
        return Result.success(commentService.listByCourse(courseId));
    }

    @PostMapping
    public Result<Void> addComment(@CurrentUserId Long uid,
                                   @RequestBody CommentDTO.AddReq req) {
        commentService.addComment(uid, req);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteComment(@CurrentUserId Long uid,
                                      @PathVariable Long id,
                                      HttpServletRequest request) {
        String role = (String) request.getAttribute(CurrentUserIdResolver.ATTR_ROLE);
        commentService.deleteComment(uid, role, id);
        return Result.success();
    }
}
