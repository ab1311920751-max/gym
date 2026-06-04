import request from '../utils/request'

export const recharge = (data) => request.post('/user/recharge', data)

export const buyVip = (data) => request.post('/user/buyVip', data)

export const getUserById = (id) => request.get(`/user/${id}`)

export const pageUsers = (params) => request.get('/user/page', { params })

export const addUser = (data) => request.post('/user', data)

export const updateUser = (data) => request.put('/user', data)

export const deleteUser = (id) => request.delete(`/user/${id}`)

export const updateProfile = (data) => request.put('/user/profile', data)

export const changePassword = (data) => request.put('/user/password', data)

export const updateUserStatus = (id, status) =>
  request.put(`/user/${id}/status`, null, { params: { status } })
