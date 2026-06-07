import request from '../utils/request'

export const listComments = (courseId) => request.get(`/comment/course/${courseId}`)
export const addComment = (data) => request.post('/comment', data)
export const deleteComment = (id) => request.delete(`/comment/${id}`)
