import request from '../utils/request'

export const getDashboard = () => request.get('/report/dashboard')
