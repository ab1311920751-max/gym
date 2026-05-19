import request from '../utils/request'

const ALIPAY_BASE = 'http://localhost:8080/alipay/pay'

export const buildPayUrl = ({ bookingNo, traceNo, totalAmount, subject }) => {
  const params = new URLSearchParams()
  if (bookingNo) params.append('bookingNo', bookingNo)
  if (traceNo) params.append('traceNo', traceNo)
  if (totalAmount != null) params.append('totalAmount', totalAmount)
  if (subject) params.append('subject', subject)
  return `${ALIPAY_BASE}?${params.toString()}`
}

export const confirmRecharge = (data) => request.post('/alipay/success', data)
