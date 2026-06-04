package com.example.gym.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.jwt.JWTUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.gym.common.exception.BusinessException;
import com.example.gym.common.exception.ErrorCode;
import com.example.gym.dto.UserDTO;
import com.example.gym.entity.SysUser;
import com.example.gym.mapper.UserMapper;
import com.example.gym.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户业务实现，继承 MyBatis-Plus ServiceImpl，实现 UserService 接口。
 * <p>
 * 核心功能包括：
 * <ul>
 *   <li>登录：BCrypt 密码校验 + 明文密码无感升级 + JWT 签发（payload 含 uid/role/exp）</li>
 *   <li>注册：role/balance/vipType 强制赋默认值，防止前端注入</li>
 *   <li>余额充值：SQL 原子累加（balance = balance + amount）</li>
 *   <li>VIP 购买：续费时在原到期时间上叠加，不损失剩余时长</li>
 *   <li>个人资料/密码修改：改用户名时检查唯一性，改密码需验证原密码</li>
 *   <li>账号状态管理：管理员不能禁用自己</li>
 * </ul>
 * 所有写操作均使用 @Transactional 保证事务一致性。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, SysUser> implements UserService {

    private static final BigDecimal PRICE_MONTHLY = new BigDecimal("30.00");
    private static final BigDecimal PRICE_YEARLY = new BigDecimal("300.00");

    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> login(SysUser user) {
        Assert.notBlank(user.getUsername(), "用户名不能为空");
        Assert.notBlank(user.getPassword(), "密码不能为空");

        SysUser dbUser = this.getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, user.getUsername()));

        if (dbUser == null || !verifyPassword(user.getPassword(), dbUser)) {
            throw new BusinessException(ErrorCode.BIZ_USERNAME_OR_PASSWORD_WRONG);
        }

        if (Integer.valueOf(0).equals(dbUser.getStatus())) {
            throw new BusinessException(ErrorCode.BIZ_USER_DISABLED);
        }

        // 登录时顺便检查 VIP 是否过期，过期则降回普通会员
        checkVipStatus(dbUser);

        // payload 包含 uid、role、exp（2小时后过期），由前端路由守卫解析 exp 判断是否需要重新登录
        Map<String, Object> payload = new HashMap<>();
        payload.put("uid", dbUser.getId());
        payload.put("role", dbUser.getRole());
        payload.put("exp", System.currentTimeMillis() / 1000 + 7200);
        String token = JWTUtil.createToken(payload, "my-secret-key".getBytes());

        return MapUtil.builder(new HashMap<String, Object>())
                .put("token", token)
                .put("user", dbUser)
                .build();
    }

    /**
     * 密码校验，兼容 BCrypt 哈希与历史明文两种存储格式。
     * 若数据库存的是明文（旧数据），验证通过后立即升级为 BCrypt 写回，
     * 不要求用户主动改密码，实现无感迁移。
     */
    private boolean verifyPassword(String raw, SysUser dbUser) {
        String stored = dbUser.getPassword();
        if (stored == null) return false;

        if (isBcryptHash(stored)) {
            return passwordEncoder.matches(raw, stored);
        }

        // 历史明文：比对通过后就地升级为 BCrypt
        if (raw.equals(stored)) {
            String upgraded = passwordEncoder.encode(raw);
            this.update(new LambdaUpdateWrapper<SysUser>()
                    .set(SysUser::getPassword, upgraded)
                    .eq(SysUser::getId, dbUser.getId()));
            dbUser.setPassword(upgraded);
            log.info("已将用户 {} 的明文密码升级为 BCrypt 哈希", dbUser.getUsername());
            return true;
        }
        return false;
    }

    // BCrypt 哈希固定 60 字符，以 $2a$/$2b$/$2y$ 开头
    private boolean isBcryptHash(String value) {
        return value.length() == 60 &&
                (value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$"));
    }

    @Override
    public void register(UserDTO.RegisterReq req) {
        Assert.notBlank(req.getUsername(), "用户名不能为空");
        Assert.notBlank(req.getPassword(), "密码不能为空");

        long count = this.count(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, req.getUsername()));
        if (count > 0) {
            throw new BusinessException(ErrorCode.BIZ_USERNAME_EXISTS);
        }

        if (req.getGender() != null) {
            Assert.isTrue(req.getGender() >= 0 && req.getGender() <= 2, "性别参数无效");
        }

        SysUser user = new SysUser();
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword())); // 注册时直接 BCrypt 加密
        user.setNickname(req.getNickname());
        user.setPhone(req.getPhone());
        user.setGender(req.getGender());
        user.setEmail(req.getEmail());
        user.setRole("user");           // 注册用户固定为普通角色，防止前端注入
        user.setBalance(BigDecimal.ZERO);
        user.setVipType(0);
        user.setVipExpireTime(null);
        user.setCreateTime(LocalDateTime.now());

        this.save(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recharge(Long userId, BigDecimal amount) {
        Assert.notNull(userId, "用户ID不能为空");
        Assert.notNull(amount, "金额不能为空");
        Assert.isTrue(amount.compareTo(BigDecimal.ZERO) > 0, "充值金额必须大于0");

        boolean success = this.update(new LambdaUpdateWrapper<SysUser>()
                .setSql("balance = balance + " + amount)
                .eq(SysUser::getId, userId));

        if (!success) {
            throw new BusinessException(ErrorCode.BIZ_USER_NOT_FOUND, "充值失败，用户不存在");
        }
    }

    /**
     * 购买 VIP。若当前 VIP 未过期，在原到期时间基础上叠加时长（续费不损失剩余时间）；
     * 否则从当前时间开始计算。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void buyVip(Long userId, Integer vipType) {
        Assert.notNull(userId, "用户ID不能为空");
        Assert.isTrue(vipType == 1 || vipType == 2, "VIP类型错误");

        SysUser user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.BIZ_USER_NOT_FOUND);
        }

        BigDecimal price = (vipType == 1) ? PRICE_MONTHLY : PRICE_YEARLY;
        int monthsToAdd = (vipType == 1) ? 1 : 12;

        if (user.getBalance().compareTo(price) < 0) {
            throw new BusinessException(ErrorCode.BIZ_BALANCE_NOT_ENOUGH);
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime newExpireTime;
        if (user.getVipType() > 0 && user.getVipExpireTime() != null && user.getVipExpireTime().isAfter(now)) {
            // 续费：在当前到期时间上叠加，不损失剩余时长
            newExpireTime = user.getVipExpireTime().plusMonths(monthsToAdd);
        } else {
            // 首次购买或已过期：从现在开始
            newExpireTime = now.plusMonths(monthsToAdd);
        }

        user.setBalance(user.getBalance().subtract(price));
        user.setVipType(vipType);
        user.setVipExpireTime(newExpireTime);
        this.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProfile(Long userId, UserDTO.UpdateProfileReq req) {
        Assert.notNull(userId, "用户ID不能为空");

        LambdaUpdateWrapper<SysUser> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SysUser::getId, userId);

        boolean hasUpdate = false;

        if (StrUtil.isNotBlank(req.getUsername())) {
            // 改用户名时检查是否与其他用户重名（排除自己）
            long count = this.count(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getUsername, req.getUsername())
                    .ne(SysUser::getId, userId));
            if (count > 0) {
                throw new BusinessException(ErrorCode.BIZ_USERNAME_EXISTS);
            }
            wrapper.set(SysUser::getUsername, req.getUsername());
            hasUpdate = true;
        }

        if (req.getNickname() != null) { wrapper.set(SysUser::getNickname, req.getNickname()); hasUpdate = true; }
        if (req.getPhone() != null)    { wrapper.set(SysUser::getPhone, req.getPhone());       hasUpdate = true; }
        if (req.getEmail() != null)    { wrapper.set(SysUser::getEmail, req.getEmail());       hasUpdate = true; }

        if (req.getGender() != null) {
            Assert.isTrue(req.getGender() >= 0 && req.getGender() <= 2, "性别参数无效");
            wrapper.set(SysUser::getGender, req.getGender());
            hasUpdate = true;
        }

        if (hasUpdate) {
            this.update(wrapper);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        Assert.notNull(userId, "用户ID不能为空");
        Assert.notBlank(oldPassword, "原密码不能为空");
        Assert.notBlank(newPassword, "新密码不能为空");

        SysUser user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.BIZ_USER_NOT_FOUND);
        }

        if (!verifyPassword(oldPassword, user)) {
            throw new BusinessException(ErrorCode.BIZ_PASSWORD_WRONG);
        }

        this.update(new LambdaUpdateWrapper<SysUser>()
                .set(SysUser::getPassword, passwordEncoder.encode(newPassword))
                .eq(SysUser::getId, userId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long operatorId, Long targetId, Integer status) {
        Assert.notNull(targetId, "用户ID不能为空");
        Assert.isTrue(status == 0 || status == 1, "状态参数无效");

        // 管理员不能禁用自己，否则会立即失去管理权限
        if (operatorId.equals(targetId)) {
            throw new BusinessException(ErrorCode.BIZ_CANNOT_DISABLE_SELF);
        }

        SysUser target = this.getById(targetId);
        if (target == null) {
            throw new BusinessException(ErrorCode.BIZ_USER_NOT_FOUND);
        }

        this.update(new LambdaUpdateWrapper<SysUser>()
                .set(SysUser::getStatus, status)
                .eq(SysUser::getId, targetId));
    }

    // VIP 过期检查：过期后降回普通会员（vipType=0），在登录时触发
    private void checkVipStatus(SysUser user) {
        if (user.getVipType() != null && user.getVipType() > 0
                && user.getVipExpireTime() != null
                && user.getVipExpireTime().isBefore(LocalDateTime.now())) {
            user.setVipType(0);
            this.updateById(user);
        }
    }
}
