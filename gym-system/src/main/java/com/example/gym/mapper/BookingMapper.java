package com.example.gym.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.gym.entity.CourseBooking;
import com.example.gym.vo.BookingVO;
import com.example.gym.vo.DailyTrendVO;
import com.example.gym.vo.RankItemVO;
import org.apache.ibatis.annotations.Param;
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
    @Select("SELECT b.id, b.status, b.booking_no as bookingNo, b.create_time as bookingTime, " +
            "c.name as courseName, c.coach, c.start_time as startTime, c.price, b.real_price as realPrice " +
            "FROM course_booking b " +
            "LEFT JOIN gym_course c ON b.course_id = c.id " +
            "WHERE b.user_id = #{userId} " +
            "ORDER BY b.create_time DESC")
    List<BookingVO> selectMyBookings(Long userId);

    @Select("SELECT DATE_FORMAT(create_time,'%m-%d') AS date, COUNT(*) AS bookingCount, " +
            "COALESCE(SUM(real_price),0) AS revenue " +
            "FROM course_booking " +
            "WHERE status=1 AND create_time >= DATE_SUB(CURDATE(), INTERVAL #{days}-1 DAY) " +
            "GROUP BY DATE_FORMAT(create_time,'%m-%d') ORDER BY DATE_FORMAT(create_time,'%m-%d')")
    List<DailyTrendVO> selectDailyTrend(@Param("days") int days);

    @Select("SELECT c.name AS name, COUNT(b.id) AS value " +
            "FROM course_booking b JOIN gym_course c ON b.course_id=c.id " +
            "WHERE b.status=1 " +
            "GROUP BY b.course_id, c.name ORDER BY value DESC LIMIT #{limit}")
    List<RankItemVO> selectCourseRank(@Param("limit") int limit);

    @Select("SELECT c.category AS name, COUNT(b.id) AS value " +
            "FROM course_booking b JOIN gym_course c ON b.course_id=c.id " +
            "WHERE b.status=1 " +
            "GROUP BY c.category ORDER BY value DESC")
    List<RankItemVO> selectCategoryStats();
}
