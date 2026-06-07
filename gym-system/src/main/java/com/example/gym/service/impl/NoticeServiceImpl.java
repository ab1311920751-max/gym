package com.example.gym.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.gym.entity.SysNotice;
import com.example.gym.mapper.NoticeMapper;
import com.example.gym.service.NoticeService;
import org.springframework.stereotype.Service;

@Service
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, SysNotice> implements NoticeService {
}
