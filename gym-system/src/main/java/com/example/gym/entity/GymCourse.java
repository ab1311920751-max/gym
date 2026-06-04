package com.example.gym.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 健身课程实体，对应数据库表 gym_course。
 * 包含课程基本信息、教练、时间、库存、价格等字段。
 * 库存扣减在 BookingServiceImpl 中通过 SQL 原子更新（stock = stock - 1 WHERE stock > 0）实现。
 */
@Data
@TableName("gym_course")
public class GymCourse {
    /** 主键，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 课程名称 */
    private String name;

    /** 教练姓名 */
    private String coach;

    /** 课程简介，用于列表展示 */
    private String description;

    /** 课程详细内容，用于详情页展示 */
    private String content;

    /** 课程分类，如"有氧训练"、"力量训练"、"瑜伽冥想"等，需与前端 COURSE_CATEGORIES 保持一致 */
    private String category;

    /** 开课时间，预约时校验是否已过期 */
    private LocalDateTime startTime;

    /** 课程最大容量 */
    private Integer capacity;

    /** 当前剩余库存（可预约人数），下单时 SQL 原子扣减（stock = stock - 1 WHERE stock > 0） */
    private Integer stock;

    /** 课程原价 */
    private BigDecimal price;

    /** 乐观锁版本号，由 MyBatis-Plus FieldFill.INSERT 自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private Integer version;
}