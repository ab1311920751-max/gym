import request from '../utils/request'

export const listBanners = () => request.get('/banner/list')
export const pageBanners = (params) => request.get('/banner/page', { params })
export const addBanner = (data) => request.post('/banner', data)
export const updateBanner = (data) => request.put('/banner', data)
export const deleteBanner = (id) => request.delete(`/banner/${id}`)
