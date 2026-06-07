import request from '../utils/request'

export const listNotices = () => request.get('/notice/list')

export const pageNotices = (params) => request.get('/notice/page', { params })

export const addNotice = (data) => request.post('/notice', data)

export const updateNotice = (data) => request.put('/notice', data)

export const deleteNotice = (id) => request.delete(`/notice/${id}`)
