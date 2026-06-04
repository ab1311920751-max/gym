package com.example.gym.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.gym.entity.Banner;
import org.apache.ibatis.annotations.Mapper;

/**
 * 轮播图 Mapper，继承 MyBatis-Plus BaseMapper&lt;Banner&gt;，自动拥有通用 CRUD 能力。
 * 暂无自定义 SQL，轮播图查询通过 LambdaQueryWrapper 在 Controller 层构建。
 */
@Mapper
public interface BannerMapper extends BaseMapper<Banner> {
}
