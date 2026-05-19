package com.example.gym.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.gym.entity.CourseBooking;
import com.example.gym.vo.BookingVO;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface BookingMapper extends BaseMapper<CourseBooking> {

    @Select("SELECT b.id, b.status, b.create_time as bookingTime, " +
            "c.name as courseName, c.coach, c.start_time as startTime, c.price " +
            "FROM course_booking b " +
            "LEFT JOIN gym_course c ON b.course_id = c.id " +
            "WHERE b.user_id = #{userId} " +
            "ORDER BY b.create_time DESC")
    List<BookingVO> selectMyBookings(Long userId);
}
