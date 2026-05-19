import request from '../utils/request'

export const sendMessage = (data) => request.post('/ai/chat', data)
export const getSessions = () => request.get('/ai/sessions')
export const getMessages = (sessionId) => request.get(`/ai/sessions/${sessionId}/messages`)
export const deleteSession = (sessionId) => request.delete(`/ai/sessions/${sessionId}`)
