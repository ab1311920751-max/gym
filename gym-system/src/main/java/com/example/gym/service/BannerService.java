package com.example.gym.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.gym.entity.Banner;

/**
 * 轮播图业务接口。
 * 目前所有操作均由 IService 提供，Controller 层负责 status 过滤和排序逻辑，
 * 后续如需添加图片上传、定时上下线等功能，在此扩展方法。
 */
public interface BannerService extends IService<Banner> {
}
