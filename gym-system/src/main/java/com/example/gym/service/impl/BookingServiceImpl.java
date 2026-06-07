package com.example.gym.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.gym.common.exception.BusinessException;
import com.example.gym.common.exception.ErrorCode;
import com.example.gym.entity.CourseBooking;
import com.example.gym.entity.GymCourse;
import com.example.gym.entity.SysUser;
import com.example.gym.entity.enums.BookingStatus;
import com.example.gym.mapper.BookingMapper;
import com.example.gym.mapper.CourseMapper;
import com.example.gym.mapper.UserMapper;
import com.example.gym.service.BookingService;
import com.example.gym.service.strategy.DiscountFactory;
import com.example.gym.vo.BookingVO;
import com.example.gym.vo.DailyTrendVO;
import com.example.gym.vo.RankItemVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 预约业务实现，继承 MyBatis-Plus ServiceImpl，实现 BookingService 接口。
 * <p>
 * 核心是 bookCourse() 的高并发抢课逻辑，使用 Redisson 分布式锁（courseId 粒度）防止超卖。
 * 锁内完成：库存校验 → 过期校验 → 重复预约校验 → 时间冲突校验
 *          → SQL 原子扣减库存（WHERE stock > 0 双重兜底）
 *          → 折扣计算（策略模式）→ 雪花算法生成订单号 → 写订单。
 * <p>
 * 取消预约时区分 PENDING（回库存）和 PAID（退款 + 回库存），
 * 使用带 ne(CANCELLED) 条件的原子更新防止并发取消导致双倍退款。
 * 余额支付使用 CAS 状态更新 + ge(balance) 原子扣款防超扣。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl extends ServiceImpl<BookingMapper, CourseBooking> implements BookingService {

    private final CourseMapper courseMapper;
    private final UserMapper userMapper;
    private final RedissonClient redissonClient;

    @Override
    public CourseBooking getBookingByNo(String bookingNo) {
        return baseMapper.selectOne(new LambdaQueryWrapper<CourseBooking>()
                .eq(CourseBooking::getBookingNo, bookingNo));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void paySuccess(String bookingNo, String alipayTradeNo) {
        CourseBooking booking = getBookingByNo(bookingNo);
        if (booking != null && BookingStatus.of(booking.getStatus()).canPay()) {
            booking.setStatus(BookingStatus.PAID.getCode());
            baseMapper.updateById(booking);
            log.info("支付宝支付成功，订单号: {}, 流水号: {}", bookingNo, alipayTradeNo);
        }
    }

    /**
     * 抢课核心方法，使用 Redisson 分布式锁防止超卖。
     *
     * 锁粒度为 courseId，不同课程互不阻塞。锁内完成所有"读-判断-写"操作：
     * 库存校验 → 过期校验 → 重复预约校验 → 时间冲突校验 → SQL 原子扣减库存 → 折扣计算 → 写订单。
     * SQL 扣减额外带 stock > 0 条件，作为数据库层兜底，防止极端并发漏判。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long bookCourse(Long userId, Long courseId) {
        Assert.notNull(userId, "用户ID不能为空");
        Assert.notNull(courseId, "课程ID不能为空");

        // courseId 粒度的锁键，不同课程并发互不影响
        String lockKey = "gym:booking:lock:" + courseId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 等待 3s 抢锁；持有上限 10s，防止进程崩溃后锁永久不释放
            boolean isLocked = lock.tryLock(3, 10, TimeUnit.SECONDS);
            if (!isLocked) {
                throw new BusinessException(ErrorCode.BIZ_LOCK_TIMEOUT);
            }

            GymCourse course = courseMapper.selectById(courseId);
            if (course == null) {
                throw new BusinessException(ErrorCode.BIZ_COURSE_NOT_FOUND);
            }
            if (course.getStock() <= 0) {
                throw new BusinessException(ErrorCode.BIZ_COURSE_SOLD_OUT);
            }
            if (course.getStartTime().isBefore(LocalDateTime.now())) {
                throw new BusinessException(ErrorCode.BIZ_COURSE_EXPIRED);
            }

            SysUser user = userMapper.selectById(userId);
            if (user == null) {
                throw new BusinessException(ErrorCode.BIZ_USER_NOT_FOUND);
            }

            // 同一用户对同一课程只允许存在一条 PENDING 或 PAID 记录
            Long repeatCount = baseMapper.selectCount(new LambdaQueryWrapper<CourseBooking>()
                    .eq(CourseBooking::getUserId, userId)
                    .eq(CourseBooking::getCourseId, courseId)
                    .in(CourseBooking::getStatus,
                            BookingStatus.PENDING.getCode(),
                            BookingStatus.PAID.getCode()));
            if (repeatCount > 0) {
                throw new BusinessException(ErrorCode.BIZ_BOOKING_DUPLICATE);
            }

            checkTimeConflict(userId, course.getStartTime());

            // WHERE stock > 0 是数据库层最后防线，即使 Java 层判断通过，SQL 层再确认一次
            int rows = courseMapper.update(null, new LambdaUpdateWrapper<GymCourse>()
                    .setSql("stock = stock - 1")
                    .eq(GymCourse::getId, courseId)
                    .gt(GymCourse::getStock, 0));
            if (rows <= 0) {
                throw new BusinessException(ErrorCode.BIZ_COURSE_SOLD_OUT);
            }

            // 根据 vipType 走策略模式计算折后价，并固化到订单，防止后续 VIP 变动影响已有订单
            BigDecimal realPrice = DiscountFactory.calculatePrice(course.getPrice(), user.getVipType());

            // bookingNo 使用雪花算法，全局唯一且不暴露自增规律
            CourseBooking booking = new CourseBooking();
            booking.setUserId(userId);
            booking.setCourseId(courseId);
            booking.setBookingNo(IdUtil.getSnowflakeNextIdStr());
            booking.setStatus(BookingStatus.PENDING.getCode());
            booking.setRealPrice(realPrice);
            booking.setCreateTime(LocalDateTime.now());

            baseMapper.insert(booking);
            return booking.getId();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "系统繁忙");
        } finally {
            // isHeldByCurrentThread() 防止锁超时自动过期后 unlock 误释放他人的锁
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    // 只检查已支付的课程是否与新课程时间相同
    private void checkTimeConflict(Long userId, LocalDateTime newStartTime) {
        List<BookingVO> existingBookings = baseMapper.selectMyBookings(userId);
        for (BookingVO record : existingBookings) {
            if (record.getStatus() == null) continue;
            BookingStatus status = BookingStatus.of(record.getStatus());
            if (status != BookingStatus.PAID) continue;
            LocalDateTime existTime = record.getStartTime();
            if (existTime != null && existTime.isEqual(newStartTime)) {
                throw new BusinessException(ErrorCode.BIZ_BOOKING_TIME_CONFLICT,
                        "时间冲突！您在 " + existTime + " 已有其他课程");
            }
        }
    }

    @Override
    public List<BookingVO> getMyBookings(Long userId) {
        List<BookingVO> list = baseMapper.selectMyBookings(userId);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        for (BookingVO vo : list) {
            if (vo.getStartTime() != null) {
                vo.setStartTimeDisplay(vo.getStartTime().format(dtf));
            }
        }
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelBooking(Long bookingId) {
        CourseBooking booking = baseMapper.selectById(bookingId);
        if (booking == null) {
            throw new BusinessException(ErrorCode.BIZ_BOOKING_NOT_FOUND);
        }

        BookingStatus status = BookingStatus.of(booking.getStatus());
        if (!status.canCancel()) {
            throw new BusinessException(ErrorCode.BIZ_BOOKING_STATUS_ILLEGAL, "订单已取消，请勿重复操作");
        }

        // 带 ne(CANCELLED) 条件的原子更新，防止并发取消导致双倍退款
        int updated = baseMapper.update(null, new LambdaUpdateWrapper<CourseBooking>()
                .set(CourseBooking::getStatus, BookingStatus.CANCELLED.getCode())
                .eq(CourseBooking::getId, bookingId)
                .ne(CourseBooking::getStatus, BookingStatus.CANCELLED.getCode()));
        if (updated == 0) {
            throw new BusinessException(ErrorCode.BIZ_BOOKING_STATUS_ILLEGAL, "订单已取消，请勿重复操作");
        }

        if (status == BookingStatus.PAID) {
            // 已支付取消：退款到余额 + 回库存
            BigDecimal refundAmount = booking.getRealPrice();
            if (refundAmount.compareTo(BigDecimal.ZERO) > 0) {
                userMapper.update(null, new LambdaUpdateWrapper<SysUser>()
                        .setSql("balance = balance + {0}", refundAmount)
                        .eq(SysUser::getId, booking.getUserId()));
            }
            courseMapper.update(null, new LambdaUpdateWrapper<GymCourse>()
                    .setSql("stock = stock + 1")
                    .eq(GymCourse::getId, booking.getCourseId()));
        } else if (status == BookingStatus.PENDING) {
            // 下单即扣库存，PENDING 状态取消同样需要回库存
            courseMapper.update(null, new LambdaUpdateWrapper<GymCourse>()
                    .setSql("stock = stock + 1")
                    .eq(GymCourse::getId, booking.getCourseId()));
        }
    }

    @Override
    public List<DailyTrendVO> getDailyTrend(int days) {
        return baseMapper.selectDailyTrend(days);
    }

    @Override
    public List<RankItemVO> getCourseRank(int limit) {
        return baseMapper.selectCourseRank(limit);
    }

    @Override
    public List<RankItemVO> getCategoryStats() {
        return baseMapper.selectCategoryStats();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payBooking(Long bookingId) {
        CourseBooking booking = baseMapper.selectById(bookingId);
        if (booking == null) {
            throw new BusinessException(ErrorCode.BIZ_BOOKING_NOT_FOUND);
        }

        BookingStatus status = BookingStatus.of(booking.getStatus());
        if (!status.canPay()) {
            throw new BusinessException(ErrorCode.BIZ_BOOKING_STATUS_ILLEGAL, "订单状态异常，无法支付");
        }

        SysUser user = userMapper.selectById(booking.getUserId());
        if (user.getBalance().compareTo(booking.getRealPrice()) < 0) {
            throw new BusinessException(ErrorCode.BIZ_BALANCE_NOT_ENOUGH);
        }

        // 先将状态从 PENDING 改为 PAID，防止并发双重支付；更新 0 行说明已被别的请求处理
        int updated = baseMapper.update(null, new LambdaUpdateWrapper<CourseBooking>()
                .set(CourseBooking::getStatus, BookingStatus.PAID.getCode())
                .eq(CourseBooking::getId, bookingId)
                .eq(CourseBooking::getStatus, BookingStatus.PENDING.getCode()));
        if (updated == 0) {
            throw new BusinessException(ErrorCode.BIZ_BOOKING_STATUS_ILLEGAL, "订单状态异常，无法支付");
        }

        // ge(balance, realPrice) 保证扣款与余额校验原子完成，防止并发扣成负数
        int rows = userMapper.update(null, new LambdaUpdateWrapper<SysUser>()
                .setSql("balance = balance - {0}", booking.getRealPrice())
                .eq(SysUser::getId, user.getId())
                .ge(SysUser::getBalance, booking.getRealPrice()));
        if (rows == 0) {
            throw new BusinessException(ErrorCode.BIZ_BALANCE_NOT_ENOUGH, "支付失败，余额发生变动");
        }
    }
}
