package com.example.gym.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.gym.entity.CourseBooking;
import com.example.gym.vo.BookingVO;

import java.util.List;

public interface BookingService extends IService<CourseBooking> {
    Long bookCourse(Long userId, Long courseId);
    List<BookingVO> getMyBookings(Long userId);
    void cancelBooking(Long bookingId);
    void payBooking(Long bookingId);

    CourseBooking getBookingByNo(String bookingNo);
    void paySuccess(String bookingNo, String alipayTradeNo);
}