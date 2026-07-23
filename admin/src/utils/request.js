import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useAdminStore } from '../stores/admin'
import router from '../router'

const service = axios.create({
  baseURL: '/api',
  timeout: 15000,
})

// 请求拦截器：自动附加 token
service.interceptors.request.use(
  (config) => {
    const adminStore = useAdminStore()
    if (adminStore.token) {
      config.headers['Authorization'] = `Bearer ${adminStore.token}`
    }
    return config
  },
  (error) => Promise.reject(error),
)

// 响应拦截器：统一处理业务码
service.interceptors.response.use(
  (response) => {
    const res = response.data
    // 文件流等非标准响应直接返回
    if (res instanceof Blob || res === undefined || res.code === undefined) {
      return res
    }
    if (res.code === 200) {
      return res
    }
    // 401 未登录
    if (res.code === 401) {
      const adminStore = useAdminStore()
      adminStore.clearAuth()
      ElMessage.error(res.message || '登录已过期，请重新登录')
      router.push('/login')
      return Promise.reject(new Error(res.message || '未登录'))
    }
    // 403 无权限
    if (res.code === 403) {
      ElMessage.error(res.message || '无权限访问')
      return Promise.reject(new Error(res.message || '无权限'))
    }
    // 其他业务错误
    ElMessage.error(res.message || '操作失败')
    return Promise.reject(new Error(res.message || '操作失败'))
  },
  (error) => {
    // HTTP 层错误
    const status = error.response?.status
    if (status === 401) {
      const adminStore = useAdminStore()
      adminStore.clearAuth()
      ElMessage.error('登录已过期，请重新登录')
      router.push('/login')
    } else if (status === 403) {
      ElMessage.error('无权限访问')
    } else {
      ElMessage.error(error.response?.data?.message || error.message || '网络异常')
    }
    return Promise.reject(error)
  },
)

export default service
