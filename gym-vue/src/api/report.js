import request from '../utils/request'

// 统计报表接口，对应后端 /report 路由

/** 获取仪表盘数据，包含用户总数、订单总数、总收入、VIP 分布等统计指标 */
export const getDashboard = () => request.get('/report/dashboard')
