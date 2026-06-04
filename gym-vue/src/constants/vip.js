/**
 * VIP 会员类型常量定义。
 * 包含类型枚举值、展示标签、Element Plus Tag 类型、价格、有效期天数、折扣标签。
 * 与后端 SysUser.vipType 字段和 DiscountFactory 折扣策略保持一致。
 */

export const VIP_TYPE = {
  NORMAL: 0,
  MONTHLY: 1,
  YEARLY: 2
}

export const VIP_LABEL = {
  [VIP_TYPE.NORMAL]: '普通会员',
  [VIP_TYPE.MONTHLY]: '月卡 VIP',
  [VIP_TYPE.YEARLY]: '年卡 VIP'
}

export const VIP_TAG_TYPE = {
  [VIP_TYPE.NORMAL]: 'info',
  [VIP_TYPE.MONTHLY]: 'warning',
  [VIP_TYPE.YEARLY]: 'danger'
}

export const VIP_PRICE = {
  [VIP_TYPE.MONTHLY]: 30,
  [VIP_TYPE.YEARLY]: 300
}

export const VIP_DURATION_DAYS = {
  [VIP_TYPE.MONTHLY]: 30,
  [VIP_TYPE.YEARLY]: 365
}

export const VIP_DISCOUNT_LABEL = {
  [VIP_TYPE.NORMAL]: '原价',
  [VIP_TYPE.MONTHLY]: '9 折',
  [VIP_TYPE.YEARLY]: '8 折'
}
