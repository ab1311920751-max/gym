import request from '../utils/request'

export const listCourses = () => request.get('/course/list')

export const pageCourses = (params) => request.get('/course/page', { params })

export const getCourse = (id) => request.get(`/course/${id}`)

export const addCourse = (data) => request.post('/course', data)

export const updateCourse = (data) => request.put('/course', data)

export const deleteCourse = (id) => request.delete(`/course/${id}`)
