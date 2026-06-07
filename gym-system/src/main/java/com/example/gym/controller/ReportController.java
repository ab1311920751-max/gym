package com.example.gym.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.gym.common.Result;
import com.example.gym.entity.CourseBooking;
import com.example.gym.entity.SysUser;
import com.example.gym.entity.enums.BookingStatus;
import com.example.gym.service.BookingService;
import com.example.gym.service.UserService;
import com.example.gym.vo.DailyTrendVO;
import com.example.gym.vo.RankItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 数据报表控制器，为管理员数据驾驶舱（Home.vue）提供统计数据。
 * 当前仅有一个 /dashboard 接口，后续可在此扩展更多报表维度。
 */
@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
public class ReportController {

    private final UserService userService;
    private final BookingService bookingService;

    /**
     * 数据驾驶舱汇总接口，一次性返回以下数据：
     * - userCount：总用户数
     * - orderCount：总订单数（含待支付、已支付、已取消）
     * - totalRevenue：实收总金额（仅计算 status=PAID 的订单）
     * - vipData：各会员等级人数分布，格式符合 ECharts 饼图数据结构
     */
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboardData() {
        Map<String, Object> map = new HashMap<>();

        // 1. 统计总用户数
        long userCount = userService.count();
        map.put("userCount", userCount);

        // 2. 统计总订单数
        long orderCount = bookingService.count();
        map.put("orderCount", orderCount);

        // 3. 统计总收入 (只计算 status=1 已支付的订单)
        // 注意：这里查出所有已支付订单，在内存中求和
        List<CourseBooking> paidBookings = bookingService.list(new LambdaQueryWrapper<CourseBooking>()
                .eq(CourseBooking::getStatus, BookingStatus.PAID.getCode()));

        BigDecimal totalRevenue = BigDecimal.ZERO;
        if (paidBookings != null && !paidBookings.isEmpty()) {
            totalRevenue = paidBookings.stream()
                    .map(CourseBooking::getRealPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        map.put("totalRevenue", totalRevenue);

        // 4. 图表数据：会员类型分布 (Pie Chart)
        long commonUser = userService.count(new LambdaQueryWrapper<SysUser>().eq(SysUser::getVipType, 0));
        long monthVip = userService.count(new LambdaQueryWrapper<SysUser>().eq(SysUser::getVipType, 1));
        long yearVip = userService.count(new LambdaQueryWrapper<SysUser>().eq(SysUser::getVipType, 2));

        // 组装成 ECharts 饼图需要的格式 {name: 'xx', value: 123}
        map.put("vipData", List.of(
                Map.of("name", "普通会员", "value", commonUser),
                Map.of("name", "月卡 VIP", "value", monthVip),
                Map.of("name", "年卡 VIP", "value", yearVip)
        ));

        // 5. 近 7 天订单与营收趋势（空日期补 0，保证前端始终拿到 7 个点）
        List<DailyTrendVO> dbTrend = bookingService.getDailyTrend(7);
        DateTimeFormatter mmdd = DateTimeFormatter.ofPattern("MM-dd");
        Map<String, DailyTrendVO> trendMap = dbTrend.stream()
                .collect(Collectors.toMap(DailyTrendVO::getDate, Function.identity()));
        List<Map<String, Object>> trendData = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            String dateKey = LocalDate.now().minusDays(i).format(mmdd);
            DailyTrendVO item = trendMap.get(dateKey);
            trendData.add(Map.of(
                    "date", dateKey,
                    "bookingCount", item != null ? item.getBookingCount() : 0,
                    "revenue", item != null ? item.getRevenue() : BigDecimal.ZERO
            ));
        }
        map.put("trendData", trendData);

        // 6. 课程热度 TOP 8
        List<RankItemVO> courseRank = bookingService.getCourseRank(8);
        map.put("courseRank", courseRank);

        // 7. 课程分类预约分布
        List<RankItemVO> categoryData = bookingService.getCategoryStats();
        map.put("categoryData", categoryData);

        return Result.success(map);
    }
}