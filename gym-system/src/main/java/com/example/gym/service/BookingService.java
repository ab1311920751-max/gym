package com.example.gym.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.gym.entity.CourseBooking;
import com.example.gym.vo.BookingVO;

import java.util.List;

/**
 * 预约业务接口，核心是 bookCourse() 的高并发抢课逻辑。
 * 继承 IService 获得通用 CRUD 能力。
 */
public interface BookingService extends IService<CourseBooking> {

    /**
     * 抢课核心方法，使用 Redisson 分布式锁（courseId 粒度）防止超卖。
     * 锁内完成：库存校验 → 过期校验 → 重复预约校验 → 时间冲突校验
     *           → SQL 原子扣减库存 → 折扣计算 → 写订单。
     *
     * @return 新建订单的主键 ID
     */
    Long bookCourse(Long userId, Long courseId);

    /**
     * 查询用户的全部预约记录，通过 Mapper XML 联表返回 BookingVO，
     * 包含课程名称、教练、开课时间等展示字段。
     */
    List<BookingVO> getMyBookings(Long userId);

    /**
     * 取消预约。
     * - PENDING → CANCELLED：回补库存
     * - PAID → CANCELLED：退款到余额 + 回补库存
     * 使用带 ne(CANCELLED) 条件的原子更新，防止并发取消导致双倍退款。
     */
    void cancelBooking(Long bookingId);

    /**
     * 余额支付预约订单，PENDING → PAID。
     * 先原子更新状态（CAS 防并发），再原子扣减余额（ge(balance) 防超扣）。
     */
    void payBooking(Long bookingId);

    /** 根据订单号（bookingNo）查询订单，支付宝回调时使用 */
    CourseBooking getBookingByNo(String bookingNo);

    /**
     * 支付宝同步回调处理，将订单状态从 PENDING 更新为 PAID。
     * 仅在 canPay() 为 true 时才执行更新，保证幂等性。
     */
    void paySuccess(String bookingNo, String alipayTradeNo);
}
