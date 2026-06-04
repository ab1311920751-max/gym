package com.example.gym.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.gym.entity.Banner;
import com.example.gym.mapper.BannerMapper;
import com.example.gym.service.BannerService;
import org.springframework.stereotype.Service;

/**
 * 轮播图业务实现，继承 MyBatis-Plus ServiceImpl，自动获得通用 CRUD 方法。
 * 当前所有轮播图操作均通过 Controller 直接调用父类方法，无需自定义业务逻辑。
 */
@Service
public class BannerServiceImpl extends ServiceImpl<BannerMapper, Banner> implements BannerService {
}
