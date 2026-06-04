package com.example.gym.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.gym.entity.CourseBooking;
import com.example.gym.vo.BookingVO;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 预约订单 Mapper，继承 MyBatis-Plus BaseMapper&lt;CourseBooking&gt;。
 * 新增自定义方法 selectMyBookings()，通过联表 SQL 查询用户的预约记录及其关联课程信息。
 */
public interface BookingMapper extends BaseMapper<CourseBooking> {

    /**
     * 联表查询用户的所有预约记录，LEFT JOIN gym_course 获取课程名称、教练、开课时间、价格等展示字段，
     * 按创建时间倒序。返回 BookingVO 列表。
     */
    @Select("SELECT b.id, b.status, b.create_time as bookingTime, " +
            "c.name as courseName, c.coach, c.start_time as startTime, c.price, b.real_price as realPrice " +
            "FROM course_booking b " +
            "LEFT JOIN gym_course c ON b.course_id = c.id " +
            "WHERE b.user_id = #{userId} " +
            "ORDER BY b.create_time DESC")
    List<BookingVO> selectMyBookings(Long userId);
}
