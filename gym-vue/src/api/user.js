import request from '../utils/request'

// 用户接口，对应后端 /user 路由
// 涉及当前用户的操作（recharge/buyVip/profile/password）uid 由后端从 JWT 解析，不需要前端传

/** 余额充值，{ amount: BigDecimal } */
export const recharge = (data) => request.post('/user/recharge', data)

/** 购买或续费 VIP，{ vipType: 1=月卡, 2=年卡 } */
export const buyVip = (data) => request.post('/user/buyVip', data)

/** 按 ID 查单个用户，管理员编辑用户时使用 */
export const getUserById = (id) => request.get(`/user/${id}`)

/** 分页查询用户列表，params: { pageNum, pageSize, username? } */
export const pageUsers = (params) => request.get('/user/page', { params })

/** 管理员新增用户，role/balance/vipType 由后端强制赋值，前端传入无效 */
export const addUser = (data) => request.post('/user', data)

/** 管理员修改用户信息（需携带 id） */
export const updateUser = (data) => request.put('/user', data)

/** 管理员删除用户 */
export const deleteUser = (id) => request.delete(`/user/${id}`)

/** 当前用户修改个人资料（昵称、手机、邮箱、性别） */
export const updateProfile = (data) => request.put('/user/profile', data)

/** 当前用户修改密码，{ oldPassword, newPassword } */
export const changePassword = (data) => request.put('/user/password', data)

/**
 * 启用/禁用用户账号，status=1 启用，status=0 禁用。
 * status 通过 query 参数传递（不是请求体），后端接收 @RequestParam。
 */
export const updateUserStatus = (id, status) =>
  request.put(`/user/${id}/status`, null, { params: { status } })
