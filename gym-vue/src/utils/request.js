import axios from 'axios'
import { ElMessage } from 'element-plus'

// 所有请求的基础配置，端口与后端 application.yml 的 server.port 保持一致
const request = axios.create({
    baseURL: 'http://localhost:8080',
    timeout: 5000
})

// 请求拦截器：自动将 localStorage 中的 token 附加到 Authorization 请求头
request.interceptors.request.use(config => {
    const token = localStorage.getItem('token')
    if (token) {
        config.headers['Authorization'] = token
    }
    return config
}, error => {
    return Promise.reject(error)
})

// 统一跳转登录页并清除本地存储，避免重复跳转（pathname 判断）
function redirectToLogin(msg) {
    ElMessage.error(msg || '登录已过期，请重新登录')
    localStorage.removeItem('user')
    localStorage.removeItem('token')
    if (window.location.pathname !== '/login') {
        window.location.href = '/login'
    }
}

// 响应拦截器：统一处理业务状态码和 HTTP 状态码
request.interceptors.response.use(
    response => {
        const res = response.data
        // 后端 code 是字符串 "200"，不是数字，需用 String() 转换后比较
        const code = String(res.code)

        if (code === '200') {
            return res
        }

        if (code === '401') {
            // 后端 token 过期或无效，清除本地数据并跳登录
            redirectToLogin(res.msg)
            return Promise.reject(new Error(res.msg || 'Unauthorized'))
        }

        ElMessage.error(res.msg || '系统错误')
        return Promise.reject(new Error(res.msg || 'Error'))
    },
    error => {
        // HTTP 层面的 401（如 token 被拦截器直接拒绝）也跳登录
        if (error.response && error.response.status === 401) {
            redirectToLogin()
        } else {
            ElMessage.error(error.message || '网络异常')
        }
        return Promise.reject(error)
    }
)

export default request
