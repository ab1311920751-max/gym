import request from '../utils/request'

// 课程接口，对应后端 /course 路由

/** 获取全部课程列表，支持 ?category=xxx 筛选，首页和课程页使用 */
export const listCourses = () => request.get('/course/list')

/** 分页查询课程，params: { pageNum, pageSize, name?, category? }，管理员后台使用 */
export const pageCourses = (params) => request.get('/course/page', { params })

/** 按 ID 查询课程详情，用于课程详情页 */
export const getCourse = (id) => request.get(`/course/${id}`)

/** 管理员新增课程 */
export const addCourse = (data) => request.post('/course', data)

/** 管理员修改课程信息（需携带 id） */
export const updateCourse = (data) => request.put('/course', data)

/** 管理员删除课程 */
export const deleteCourse = (id) => request.delete(`/course/${id}`)
