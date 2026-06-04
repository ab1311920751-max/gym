package com.example.gym.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 首页轮播图实体，对应数据库表 banner_info。
 * 管理员可配置轮播图的标题、图片、排序等。首页 GET /banner/list 只返回 status=1（启用）的 Banner。
 */
@Data
@TableName("banner_info")
public class Banner {
    /** 主键，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 轮播图标题 */
    private String title;

    /** 轮播图副标题 */
    private String subtitle;

    /** 轮播图图片 URL */
    private String imageUrl;

    /** 排序序号，数值越小越靠前 */
    private Integer sort;

    /** 状态：0-禁用，1-启用 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;
}
