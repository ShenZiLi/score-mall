import request from '../utils/request'

export function listProducts(params) {
  return request.get('/admin/products', { params })
}

export function getProduct(id) {
  return request.get(`/admin/products/${id}`)
}

export function createProduct(data) {
  return request.post('/admin/products', data)
}

export function updateProduct(id, data) {
  return request.put(`/admin/products/${id}`, data)
}

export function deleteProduct(id) {
  return request.delete(`/admin/products/${id}`)
}

export function updateProductStock(id, stock) {
  return request.put(`/admin/products/${id}/stock`, null, { params: { stock } })
}
