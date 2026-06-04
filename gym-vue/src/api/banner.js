import request from '../utils/request'

// 轮播图接口，对应后端 /banner 路由
// GET /list 为白名单接口，无需登录即可访问

/** 获取启用的轮播图列表，首页使用 */
export const listBanners = () => request.get('/banner/list')

/** 分页查询轮播图（含已禁用），管理员后台使用 */
export const pageBanners = (params) => request.get('/banner/page', { params })

/** 管理员新增轮播图 */
export const addBanner = (data) => request.post('/banner', data)

/** 管理员修改轮播图（含启用/禁用切换） */
export const updateBanner = (data) => request.put('/banner', data)

/** 管理员删除轮播图 */
export const deleteBanner = (id) => request.delete(`/banner/${id}`)
