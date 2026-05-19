export const BOOKING_STATUS = {
  PENDING: 0,
  PAID: 1,
  CANCELLED: 2
}

export const BOOKING_STATUS_LABEL = {
  [BOOKING_STATUS.PENDING]: '待支付',
  [BOOKING_STATUS.PAID]: '已预约',
  [BOOKING_STATUS.CANCELLED]: '已取消'
}

export const BOOKING_STATUS_TAG_TYPE = {
  [BOOKING_STATUS.PENDING]: 'warning',
  [BOOKING_STATUS.PAID]: 'success',
  [BOOKING_STATUS.CANCELLED]: 'info'
}

export const canPay = (status) => status === BOOKING_STATUS.PENDING
export const canCancel = (status) =>
  status === BOOKING_STATUS.PENDING || status === BOOKING_STATUS.PAID

export const LOW_STOCK_THRESHOLD = 3
