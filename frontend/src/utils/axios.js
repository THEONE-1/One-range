import axios from 'axios'

// 配置 axios 默认选项
axios.defaults.withCredentials = true // 跨域请求携带 cookie/session
axios.defaults.timeout = 10000 // 10秒超时

// 响应拦截器 - 统一处理错误
axios.interceptors.response.use(
  response => response,
  error => {
    if (error.response) {
      switch (error.response.status) {
        case 401:
          console.error('未授权，请先登录')
          break
        case 403:
          console.error('没有权限访问')
          break
        case 404:
          console.error('请求的资源不存在')
          break
        case 500:
          console.error('服务器错误')
          break
        default:
          console.error('请求失败:', error.message)
      }
    } else {
      console.error('网络错误:', error.message)
    }
    return Promise.reject(error)
  }
)

export default axios
