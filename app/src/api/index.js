import request from './request'

/* ===================== 鉴权 ===================== */
export const login = (data) => request.post('/app/auth/login', data)
export const register = (data) => request.post('/app/auth/register', data)
export const getUserInfo = () => request.get('/app/auth/info')
export const updateProfile = (params) =>
  request.put('/app/auth/profile', null, { params })
export const changePassword = (data) => request.put('/app/auth/password', data)

/* ===================== 首页/商品 ===================== */
export const getHome = () => request.get('/app/home')
export const getBanners = () => request.get('/app/banners')
export const getCategories = () => request.get('/app/categories')
export const getProducts = (params) => request.get('/app/products', { params })
export const getProductDetail = (id) => request.get(`/app/products/${id}`)
export const getHotProducts = (limit = 6) =>
  request.get('/app/products/hot', { params: { limit } })
export const getRecommendProducts = (limit = 8) =>
  request.get('/app/products/recommend', { params: { limit } })

/* ===================== 订单 ===================== */
export const createOrder = (data) => request.post('/app/orders', data)
export const getOrders = (params) => request.get('/app/orders', { params })
export const getOrderDetail = (id) => request.get(`/app/orders/${id}`)
export const cancelOrder = (id) => request.post(`/app/orders/${id}/cancel`)
export const confirmOrder = (id) => request.post(`/app/orders/${id}/confirm`)

/* ===================== 积分 ===================== */
export const sign = () => request.post('/app/points/sign')
export const getSignStatus = () => request.get('/app/points/sign/status')
export const getPointsLogs = (params) =>
  request.get('/app/points/logs', { params })

/* ===================== 地址 ===================== */
export const getAddresses = () => request.get('/app/addresses')
export const getAddressDetail = (id) => request.get(`/app/addresses/${id}`)
export const addAddress = (data) => request.post('/app/addresses', data)
export const updateAddress = (id, data) =>
  request.put(`/app/addresses/${id}`, data)
export const deleteAddress = (id) => request.delete(`/app/addresses/${id}`)
export const setDefaultAddress = (id) =>
  request.post(`/app/addresses/${id}/default`)

/* ===================== 上传 ===================== */
export const uploadFile = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/app/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}
