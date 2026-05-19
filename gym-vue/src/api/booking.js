import request from '../utils/request'

export const createBooking = (data) => request.post('/booking/create', data)

export const listMyBookings = () => request.get('/booking/my')

export const payBooking = (id) => request.post(`/booking/pay/${id}`)

export const cancelBooking = (id) => request.post(`/booking/cancel/${id}`)
