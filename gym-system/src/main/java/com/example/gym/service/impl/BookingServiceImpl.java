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
     * [Core Logic] 高并发抢课核心方法
     * 1. 使用 Redisson 分布式锁防止超卖
     * 2. 校验：库存、重复预约、时间冲突
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long bookCourse(Long userId, Long courseId) {
        Assert.notNull(userId, "用户ID不能为空");
        Assert.notNull(courseId, "课程ID不能为空");

        String lockKey = "gym:booking:lock:" + courseId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
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

            // SQL 层面再加 stock > 0 兜底，防止并发漏判
            int rows = courseMapper.update(null, new LambdaUpdateWrapper<GymCourse>()
                    .setSql("stock = stock - 1")
                    .eq(GymCourse::getId, courseId)
                    .gt(GymCourse::getStock, 0));

            if (rows <= 0) {
                throw new BusinessException(ErrorCode.BIZ_COURSE_SOLD_OUT);
            }

            BigDecimal realPrice = DiscountFactory.calculatePrice(course.getPrice(), user.getVipType());

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
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

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
            throw new BusinessException(ErrorCode.BIZ_BOOKING_STATUS_ILLEGAL,
                    "订单已取消，请勿重复操作");
        }

        // 原子状态转移：防止并发取消导致双倍退款/回库存
        int updated = baseMapper.update(null, new LambdaUpdateWrapper<CourseBooking>()
                .set(CourseBooking::getStatus, BookingStatus.CANCELLED.getCode())
                .eq(CourseBooking::getId, bookingId)
                .ne(CourseBooking::getStatus, BookingStatus.CANCELLED.getCode()));
        if (updated == 0) {
            throw new BusinessException(ErrorCode.BIZ_BOOKING_STATUS_ILLEGAL,
                    "订单已取消，请勿重复操作");
        }

        if (status == BookingStatus.PAID) {
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
            // 抢课逻辑是"下单即扣库存"，PENDING 取消也要回库存
            courseMapper.update(null, new LambdaUpdateWrapper<GymCourse>()
                    .setSql("stock = stock + 1")
                    .eq(GymCourse::getId, booking.getCourseId()));
        }
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
            throw new BusinessException(ErrorCode.BIZ_BOOKING_STATUS_ILLEGAL,
                    "订单状态异常，无法支付");
        }

        SysUser user = userMapper.selectById(booking.getUserId());
        if (user.getBalance().compareTo(booking.getRealPrice()) < 0) {
            throw new BusinessException(ErrorCode.BIZ_BALANCE_NOT_ENOUGH);
        }

        // 原子状态转移：先抢占 PENDING→PAID，防止并发双重支付
        int updated = baseMapper.update(null, new LambdaUpdateWrapper<CourseBooking>()
                .set(CourseBooking::getStatus, BookingStatus.PAID.getCode())
                .eq(CourseBooking::getId, bookingId)
                .eq(CourseBooking::getStatus, BookingStatus.PENDING.getCode()));
        if (updated == 0) {
            throw new BusinessException(ErrorCode.BIZ_BOOKING_STATUS_ILLEGAL,
                    "订单状态异常，无法支付");
        }

        int rows = userMapper.update(null, new LambdaUpdateWrapper<SysUser>()
                .setSql("balance = balance - {0}", booking.getRealPrice())
                .eq(SysUser::getId, user.getId())
                .ge(SysUser::getBalance, booking.getRealPrice()));

        if (rows == 0) {
            throw new BusinessException(ErrorCode.BIZ_BALANCE_NOT_ENOUGH,
                    "支付失败，余额发生变动");
        }
    }
}
