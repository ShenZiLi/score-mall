import axios from 'axios'
import { showToast } from 'vant'
import { useUserStore } from '../store/user'
import router from '../router'

const service = axios.create({
  baseURL: '/api',
  timeout: 15000,
})

// 请求拦截器：自动加 token
service.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    return config
  },
  (error) => Promise.reject(error),
)

// 响应拦截器：统一处理业务 code
service.interceptors.response.use(
  (response) => {
    const res = response.data
    // 非标准结构（如文件下载）直接返回
    if (res && typeof res === 'object' && 'code' in res) {
      if (res.code === 200) {
        return res.data
      }
      if (res.code === 401) {
        const userStore = useUserStore()
        userStore.clear()
        showToast(res.message || '登录已失效，请重新登录')
        router.replace({
          path: '/login',
          query: { redirect: router.currentRoute.value.fullPath },
        })
        return Promise.reject(new Error(res.message || '未登录'))
      }
      showToast(res.message || '操作失败')
      return Promise.reject(new Error(res.message || '操作失败'))
    }
    return res
  },
  (error) => {
    const status = error.response && error.response.status
    if (status === 401) {
      const userStore = useUserStore()
      userStore.clear()
      showToast('登录已失效，请重新登录')
      router.replace({ path: '/login' })
    } else if (status === 404) {
      showToast('请求资源不存在')
    } else {
      showToast(
        (error.response && error.response.data && error.response.data.message) ||
          error.message ||
          '网络异常',
      )
    }
    return Promise.reject(error)
  },
)

export default service
