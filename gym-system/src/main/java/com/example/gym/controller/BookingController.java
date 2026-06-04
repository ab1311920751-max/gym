package com.example.gym.controller;

import com.example.gym.common.Result;
import com.example.gym.common.auth.CurrentUserId;
import com.example.gym.common.exception.BusinessException;
import com.example.gym.common.exception.ErrorCode;
import com.example.gym.common.exception.UnauthorizedException;
import com.example.gym.dto.BookingDTO;
import com.example.gym.entity.CourseBooking;
import com.example.gym.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 预约控制器，处理课程预约、取消和余额支付。
 * 取消和支付操作前通过 assertOwner() 校验订单归属，防止横向越权（IDOR）。
 * 预约核心逻辑（分布式锁、超卖防止）在 BookingServiceImpl.bookCourse() 中实现。
 */
@RestController
@RequestMapping("/booking")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    /**
     * 创建预约订单，返回新订单 ID（字符串格式）。
     * 实际的库存扣减和折扣计算在 service 层的分布式锁内完成。
     */
    @PostMapping("/create")
    public Result<String> createBooking(@CurrentUserId Long uid,
                                @RequestBody BookingDTO.CreateReq req) {
        Long bookingId = bookingService.bookCourse(uid, req.getCourseId());
        return Result.success(bookingId.toString());
    }

    /** 查询当前用户的所有预约记录，包含课程名称、教练、时间等关联字段 */
    @GetMapping("/my")
    public Result<java.util.List<com.example.gym.vo.BookingVO>> getMyBookings(@CurrentUserId Long uid) {
        return Result.success(bookingService.getMyBookings(uid));
    }

    /**
     * 取消预约。已支付的订单取消后自动退款到余额并回补库存；
     * 未支付的订单取消同样会回补库存（下单即扣库存）。
     */
    @PostMapping("/cancel/{id}")
    public Result<Void> cancelBooking(@CurrentUserId Long uid, @PathVariable Long id) {
        assertOwner(uid, id);
        bookingService.cancelBooking(id);
        return Result.success();
    }

    /** 余额支付预约，扣款与状态更新在同一事务内原子完成 */
    @PostMapping("/pay/{id}")
    public Result<Void> payBooking(@CurrentUserId Long uid, @PathVariable Long id) {
        assertOwner(uid, id);
        bookingService.payBooking(id);
        return Result.success();
    }

    /** 校验当前登录用户是否为订单拥有者，防止用户操作他人订单 */
    private void assertOwner(Long uid, Long bookingId) {
        CourseBooking booking = bookingService.getById(bookingId);
        if (booking == null) {
            throw new BusinessException(ErrorCode.BIZ_BOOKING_NOT_FOUND);
        }
        if (!uid.equals(booking.getUserId())) {
            throw new UnauthorizedException("无权操作他人订单");
        }
    }
}
