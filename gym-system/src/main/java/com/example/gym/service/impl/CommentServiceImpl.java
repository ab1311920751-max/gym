package com.example.gym.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.gym.common.exception.BusinessException;
import com.example.gym.common.exception.ErrorCode;
import com.example.gym.dto.CommentDTO;
import com.example.gym.entity.CourseComment;
import com.example.gym.entity.SysUser;
import com.example.gym.mapper.CourseCommentMapper;
import com.example.gym.mapper.UserMapper;
import com.example.gym.service.CommentService;
import com.example.gym.vo.CourseCommentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CourseCommentMapper commentMapper;
    private final UserMapper userMapper;

    @Override
    public List<CourseCommentVO> listByCourse(Long courseId) {
        // 查出该课程所有评论，按创建时间正序
        List<CourseComment> all = commentMapper.selectList(
                new LambdaQueryWrapper<CourseComment>()
                        .eq(CourseComment::getCourseId, courseId)
                        .orderByAsc(CourseComment::getCreateTime)
        );

        if (all.isEmpty()) {
            return Collections.emptyList();
        }

        // 批量查询所有评论者信息
        Set<Long> userIds = all.stream().map(CourseComment::getUserId).collect(Collectors.toSet());
        Map<Long, SysUser> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(SysUser::getId, u -> u));

        // 将所有评论转为 VO，并建立 id → VO 映射（用于查父评论用户名）
        Map<Long, CourseCommentVO> voMap = new LinkedHashMap<>();
        for (CourseComment c : all) {
            CourseCommentVO vo = toVO(c, userMap);
            voMap.put(c.getId(), vo);
        }

        // 填充 parentUsername，并组装树形结构
        List<CourseCommentVO> topLevel = new ArrayList<>();
        for (CourseComment c : all) {
            CourseCommentVO vo = voMap.get(c.getId());
            if (c.getParentId() == null) {
                vo.setReplies(new ArrayList<>());
                topLevel.add(vo);
            } else {
                // 填充被回复用户名
                CourseCommentVO parent = voMap.get(c.getParentId());
                if (parent != null) {
                    vo.setParentUsername(parent.getUsername());
                    parent.getReplies().add(vo);
                }
            }
        }

        return topLevel;
    }

    @Override
    public void addComment(Long uid, CommentDTO.AddReq req) {
        CourseComment comment = new CourseComment();
        comment.setCourseId(req.getCourseId());
        comment.setUserId(uid);
        comment.setContent(req.getContent().trim());
        comment.setParentId(req.getParentId());
        commentMapper.insert(comment);
    }

    @Override
    @Transactional
    public void deleteComment(Long uid, String role, Long id) {
        CourseComment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        if (!"admin".equals(role) && !comment.getUserId().equals(uid)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        // 删除该评论
        commentMapper.deleteById(id);
        // 级联删除子评论
        commentMapper.delete(
                new LambdaQueryWrapper<CourseComment>().eq(CourseComment::getParentId, id)
        );
    }

    private CourseCommentVO toVO(CourseComment c, Map<Long, SysUser> userMap) {
        CourseCommentVO vo = new CourseCommentVO();
        vo.setId(c.getId());
        vo.setCourseId(c.getCourseId());
        vo.setParentId(c.getParentId());
        vo.setContent(c.getContent());
        vo.setCreateTime(c.getCreateTime());
        vo.setUserId(c.getUserId());

        SysUser user = userMap.get(c.getUserId());
        if (user != null) {
            vo.setUsername(user.getUsername());
            vo.setNickname(user.getNickname());
        }
        return vo;
    }
}
