/**
 * 预约状态常量定义。
 * 包含状态枚举值、展示标签、Element Plus Tag 类型映射，
 * 以及状态判断工具函数和库存阈值常量。
 * 与后端 BookingStatus 枚举保持一致。
 */

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

/** 仅 PENDING 状态可支付 */
export const canPay = (status) => status === BOOKING_STATUS.PENDING

/** PENDING 和 PAID 状态可取消 */
export const canCancel = (status) =>
  status === BOOKING_STATUS.PENDING || status === BOOKING_STATUS.PAID

/** 库存低于该阈值时前端显示"紧张"标识 */
export const LOW_STOCK_THRESHOLD = 3
