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

@RestController
@RequestMapping("/booking")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping("/create")
    public Result createBooking(@CurrentUserId Long uid,
                                @RequestBody BookingDTO.CreateReq req) {
        Long bookingId = bookingService.bookCourse(uid, req.getCourseId());
        return Result.success(bookingId.toString());
    }

    @GetMapping("/my")
    public Result getMyBookings(@CurrentUserId Long uid) {
        return Result.success(bookingService.getMyBookings(uid));
    }

    @PostMapping("/cancel/{id}")
    public Result cancelBooking(@CurrentUserId Long uid, @PathVariable Long id) {
        assertOwner(uid, id);
        bookingService.cancelBooking(id);
        return Result.success();
    }

    @PostMapping("/pay/{id}")
    public Result payBooking(@CurrentUserId Long uid, @PathVariable Long id) {
        assertOwner(uid, id);
        bookingService.payBooking(id);
        return Result.success();
    }

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
