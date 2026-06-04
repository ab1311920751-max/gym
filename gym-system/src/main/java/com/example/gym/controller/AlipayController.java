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

/**
 * 支付宝支付控制器，支持两种支付场景：
 *   - 模式A：课程预约支付，传 bookingNo，支付成功跳转到 /pay/success
 *   - 模式B：余额充值，传 traceNo + totalAmount，支付成功跳转到 /wallet
 *
 * 回调说明：
 *   - 同步回调（return_url）：支付完成后浏览器跳转，用于页面跳转体验，不可作为支付确认依据
 *   - 异步回调（notify_url）：支付宝服务器主动 POST，才是可靠的支付确认来源
 *   本地开发环境 notify_url 指向 localhost，支付宝无法访问，故依赖 return_url + 前端主动确认。
 */
@Slf4j
@RestController
@RequestMapping("/alipay")
public class AlipayController {

    @Resource
    private AliPayConfig aliPayConfig;

    @Resource
    private BookingService bookingService;

    @Resource
    private UserService userService;

    /**
     * 统一支付入口，根据参数自动区分课程支付和余额充值。
     * 响应体是支付宝返回的 HTML 表单，直接写入 response，浏览器自动跳转到支付宝收银台。
     */
    @GetMapping("/pay")
    public void pay(@RequestParam(required = false) String bookingNo,
                    @RequestParam(required = false) String traceNo,
                    @RequestParam(required = false) Double totalAmount,
                    @RequestParam(required = false) String subject,
                    HttpServletResponse httpResponse) throws IOException {

        httpResponse.setContentType("text/html;charset=UTF-8");

        try {
            AlipayClient alipayClient = new DefaultAlipayClient(aliPayConfig.alipayConfig());
            AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
            request.setNotifyUrl(aliPayConfig.getNotifyUrl());

            JSONObject bizContent = new JSONObject();
            bizContent.set("product_code", "FAST_INSTANT_TRADE_PAY");

            if (bookingNo != null) {
                // 模式A：课程预约支付
                CourseBooking booking = bookingService.getBookingByNo(bookingNo);
                if (booking == null) {
                    httpResponse.getWriter().write("订单不存在");
                    return;
                }
                bizContent.set("out_trade_no", booking.getBookingNo());
                bizContent.set("total_amount", booking.getRealPrice());
                bizContent.set("subject", "健身课程预约-" + bookingNo);
                request.setReturnUrl(aliPayConfig.getReturnUrl()); // 跳转到 /pay/success

            } else if (traceNo != null && totalAmount != null) {
                // 模式B：余额充值
                bizContent.set("out_trade_no", traceNo);
                bizContent.set("total_amount", totalAmount);
                bizContent.set("subject", subject != null ? subject : "余额充值");
                // 充值成功后跳回钱包页，端口变更时需同步修改
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
     * 课程预约支付成功的同步回调，由前端 PaySuccess.vue 在跳转后调用，
     * 将订单状态从 PENDING 更新为 PAID。
     */
    @GetMapping("/return")
    public Result<Void> returnCallback(@RequestParam("out_trade_no") String outTradeNo,
                                 @RequestParam("trade_no") String tradeNo) {
        log.info("课程支付回调: 订单号={}", outTradeNo);
        bookingService.paySuccess(outTradeNo, tradeNo);
        return Result.success();
    }

    /**
     * 余额充值支付成功后由前端 Wallet.vue 主动调用。
     * userId 从 JWT 取而非请求体，防止前端伪造 userId 给他人充值。
     */
    @PostMapping("/success")
    public Result<Void> rechargeSuccess(@CurrentUserId Long uid,
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
