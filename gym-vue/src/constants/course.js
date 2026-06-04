/**
 * 课程相关常量定义。
 * 包含课程分类选项、课程状态筛选选项、排序选项，
 * 供课程列表页的筛选和排序组件使用。
 * 分类 value 需与后端 GymCourse.category 字段值保持一致。
 */

export const COURSE_CATEGORIES = [
  { label: '全部', value: '' },
  { label: '有氧训练', value: '有氧训练' },
  { label: '力量训练', value: '力量训练' },
  { label: '瑜伽冥想', value: '瑜伽冥想' },
  { label: '格斗搏击', value: '格斗搏击' },
  { label: '舞蹈健身', value: '舞蹈健身' },
  { label: '功能性训练', value: '功能性训练' },
]

export const STATUS_OPTIONS = [
  { label: '全部', value: '' },
  { label: '可预约', value: 'available' },
  { label: '已售罄', value: 'soldOut' },
  { label: '已结束', value: 'expired' },
]

export const SORT_OPTIONS = [
  { label: '最近开课', value: 'time' },
  { label: '价格最低', value: 'price' },
]
