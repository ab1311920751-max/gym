package com.example.gym.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("gym_course")
public class GymCourse {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    // --- 修复：对应数据库的 coach 字段 ---
    private String coach;

    // --- 修复：对应数据库的 description 字段 ---
    private String description;

    // --- 修复：对应数据库的 content 字段 ---
    private String content;

    private String category; // 课程分类

    // --- 核心修复：数据库是 start_time，Java必须用 startTime ---
    // 之前写的是 String times，导致报错
    private LocalDateTime startTime;

    private Integer capacity; // 最大人数

    private Integer stock;      // 库存
    private BigDecimal price;   // 价格

    //@Version
    @TableField(fill = FieldFill.INSERT) // 这里的配置有时候会坑
    private Integer version;    // 乐观锁
}