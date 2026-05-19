package com.example.gym.controller;

import cn.hutool.json.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.example.gym.common.Result;
import com.example.gym.common.auth.CurrentUserId;
import com.example.gym.config.AliPayConfig;
import com.example.gym.entity.CourseBooking;
import com.example.gym.service.BookingService;
import com.example.gym.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/alipay")
public class AlipayController {

    @Resource
    private AliPayConfig aliPayConfig;

    @Resource
    private BookingService bookingService;

    @Resource
    private UserService userService; // 用于充值余额

    /**
     * 统一支付接口：支持“课程预约”和“余额充值”两种模式
     * 1. 课程预约：传 bookingNo
     * 2. 余额充值：传 traceNo, totalAmount, subject
     */
    @GetMapping("/pay")
    public void pay(@RequestParam(required = false) String bookingNo,
                    @RequestParam(required = false) String traceNo,
                    @RequestParam(required = false) Double totalAmount,
                    @RequestParam(required = false) String subject,
                    HttpServletResponse httpResponse) throws IOException {

        // 设置响应类型，防止中文乱码
        httpResponse.setContentType("text/html;charset=UTF-8");

        try {
            AlipayClient alipayClient = new DefaultAlipayClient(aliPayConfig.alipayConfig());
            AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
            request.setNotifyUrl(aliPayConfig.getNotifyUrl());

            JSONObject bizContent = new JSONObject();
            bizContent.set("product_code", "FAST_INSTANT_TRADE_PAY");

            // --- 模式判断 ---
            if (bookingNo != null) {
                // 模式A: 课程预约支付
                CourseBooking booking = bookingService.getBookingByNo(bookingNo);
                if (booking == null) {
                    httpResponse.getWriter().write("订单不存在");
                    return;
                }
                bizContent.set("out_trade_no", booking.getBookingNo());
                bizContent.set("total_amount", booking.getRealPrice());
                bizContent.set("subject", "健身课程预约-" + bookingNo);
                // 支付成功后跳回前端订单页
                request.setReturnUrl(aliPayConfig.getReturnUrl());

            } else if (traceNo != null && totalAmount != null) {
                // 模式B: 余额充值 (修复了之前的 500 错误)
                bizContent.set("out_trade_no", traceNo);
                bizContent.set("total_amount", totalAmount); // 使用前端传来的金额
                bizContent.set("subject", subject != null ? subject : "余额充值");

                // 支付成功后跳回前端钱包页 (关键！)
                // 注意：这里硬编码了前端地址，如果端口变了记得修改
                request.setReturnUrl("http://localhost:5173/wallet?pay=success");

            } else {
                httpResponse.getWriter().write("参数错误：缺少 bookingNo 或 traceNo/totalAmount");
                return;
            }

            request.setBizContent(bizContent.toString());
            String formHtml = alipayClient.pageExecute(request).getBody();
            httpResponse.getWriter().write(formHtml);

        } catch (AlipayApiException e) {
            log.error("支付宝调用失败", e);
            httpResponse.getWriter().write("支付发起失败: " + e.getMessage());
        }
    }

    /**
     * 课程预约 - 支付成功回调 (由前端 PaySuccess.vue 调用)
     */
    @GetMapping("/return")
    public Result returnCallback(@RequestParam("out_trade_no") String outTradeNo,
                                 @RequestParam("trade_no") String tradeNo) {
        log.info("课程支付回调: 订单号={}", outTradeNo);
        bookingService.paySuccess(outTradeNo, tradeNo);
        return Result.success();
    }

    /**
     * 余额充值 - 充值成功回调 (新增接口，由前端 Wallet.vue 调用)。
     * userId 从 JWT 取，避免前端伪造给他人充值。
     */
    @PostMapping("/success")
    public Result rechargeSuccess(@CurrentUserId Long uid,
                                  @RequestBody Map<String, Object> params) {
        String traceNo = (String) params.get("out_trade_no");
        Object totalAmountObj = params.get("total_amount");
        if (totalAmountObj == null) {
            return Result.error("缺少 total_amount 参数，请重新充值");
        }
        BigDecimal amount = new BigDecimal(totalAmountObj.toString());

        log.info("用户 {} 充值回调: 金额={}, 流水号={}", uid, amount, traceNo);

        userService.recharge(uid, amount);

        return Result.success();
    }
}