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
