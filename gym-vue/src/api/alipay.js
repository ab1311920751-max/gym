import request from '../utils/request'

// 支付宝支付接口，跳转支付宝页面和确认支付结果
// 注意：本地开发环境 notify_url 不可达，依赖 return_url 同步跳转 + 前端主动确认

const ALIPAY_BASE = 'http://localhost:8080/alipay/pay'

/**
 * 构建支付宝支付页面 URL，前端通过 window.location.href 跳转。
 * 根据参数自动区分课程支付（传 bookingNo）和余额充值（传 traceNo + totalAmount）。
 * @param {Object} options
 * @param {string} [options.bookingNo] - 课程订单号
 * @param {string} [options.traceNo] - 充值流水号
 * @param {number} [options.totalAmount] - 充值金额
 * @param {string} [options.subject] - 商品名称
 * @returns {string} 支付宝收银台页面 URL
 */
export const buildPayUrl = ({ bookingNo, traceNo, totalAmount, subject }) => {
  const params = new URLSearchParams()
  if (bookingNo) params.append('bookingNo', bookingNo)
  if (traceNo) params.append('traceNo', traceNo)
  if (totalAmount != null) params.append('totalAmount', totalAmount)
  if (subject) params.append('subject', subject)
  return `${ALIPAY_BASE}?${params.toString()}`
}

/** 确认充值支付结果，支付宝同步回调后调用，{ out_trade_no, total_amount } */
export const confirmRecharge = (data) => request.post('/alipay/success', data)
