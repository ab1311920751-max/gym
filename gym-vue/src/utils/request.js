import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
    baseURL: 'http://localhost:8080',
    timeout: 5000
})

request.interceptors.request.use(config => {
    const token = localStorage.getItem('token')
    if (token) {
        config.headers['Authorization'] = token
    }
    return config
}, error => {
    return Promise.reject(error)
})

function redirectToLogin(msg) {
    ElMessage.error(msg || '登录已过期，请重新登录')
    localStorage.removeItem('user')
    localStorage.removeItem('token')
    if (window.location.pathname !== '/login') {
        window.location.href = '/login'
    }
}

request.interceptors.response.use(
    response => {
        const res = response.data
        const code = String(res.code)

        if (code === '200') {
            return res
        }

        if (code === '401') {
            redirectToLogin(res.msg)
            return Promise.reject(new Error(res.msg || 'Unauthorized'))
        }

        ElMessage.error(res.msg || '系统错误')
        return Promise.reject(new Error(res.msg || 'Error'))
    },
    error => {
        if (error.response && error.response.status === 401) {
            redirectToLogin()
        } else {
            ElMessage.error(error.message || '网络异常')
        }
        return Promise.reject(error)
    }
)

export default request
