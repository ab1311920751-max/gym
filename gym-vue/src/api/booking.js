import request from '../utils/request'

// 预约接口，对应后端 /booking 路由

/** 创建预约订单，{ courseId: Long }，返回新订单 ID */
export const createBooking = (data) => request.post('/booking/create', data)

/** 查询当前用户的所有预约记录，包含课程名称、教练、时间等关联字段 */
export const listMyBookings = () => request.get('/booking/my')

/** 余额支付预约订单，仅 PENDING 状态可支付 */
export const payBooking = (id) => request.post(`/booking/pay/${id}`)

/** 取消预约，PENDING 状态回库存，PAID 状态退款+回库存 */
export const cancelBooking = (id) => request.post(`/booking/cancel/${id}`)
