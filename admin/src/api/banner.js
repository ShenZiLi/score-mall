import request from '../utils/request'

export function listBanners() {
  return request.get('/admin/banners')
}

export function createBanner(data) {
  return request.post('/admin/banners', data)
}

export function updateBanner(id, data) {
  return request.put(`/admin/banners/${id}`, data)
}

export function deleteBanner(id) {
  return request.delete(`/admin/banners/${id}`)
}
