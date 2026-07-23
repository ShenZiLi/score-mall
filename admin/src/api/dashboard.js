import request from '../utils/request'

export function getOverview() {
  return request.get('/admin/dashboard/overview')
}

export function getOrderStatus() {
  return request.get('/admin/dashboard/order-status')
}

export function getPointsTrend(days = 30) {
  return request.get('/admin/dashboard/points-trend', { params: { days } })
}

export function getTopProducts(limit = 10) {
  return request.get('/admin/dashboard/top-products', { params: { limit } })
}
