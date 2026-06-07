package com.example.gym.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("system_notice")
public class SysNotice {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String content;

    /** 状态：0-草稿，1-已发布 */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
