import request from '../utils/request'

export function listOrders(params) {
  return request.get('/admin/orders', { params })
}

export function getOrder(id) {
  return request.get(`/admin/orders/${id}`)
}

export function shipOrder(id, shipNo) {
  return request.post(`/admin/orders/${id}/ship`, null, { params: { shipNo } })
}

export function updateOrderRemark(id, remark) {
  return request.put(`/admin/orders/${id}/remark`, null, { params: { remark } })
}
