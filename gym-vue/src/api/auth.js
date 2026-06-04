import request from '../utils/request'

// 认证接口，对应后端 /auth 路由，均为白名单接口，无需 token

/** 登录，返回 { token, user }，前端存入 localStorage 后路由守卫方可放行 */
export const login = (data) => request.post('/auth/login', data)

/** 注册，成功后不自动登录，需跳转登录页手动登录 */
export const register = (data) => request.post('/auth/register', data)
