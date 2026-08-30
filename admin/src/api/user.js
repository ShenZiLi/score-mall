import request from '../utils/request'

export function listUsers(params) {
  return request.get('/admin/users', { params })
}

export function updateUserStatus(id, status) {
  return request.put(`/admin/users/${id}/status`, null, { params: { status } })
}

export function adjustUserPoints(id, points, remark) {
  return request.post(`/admin/users/${id}/points`, null, {
    params: { points, remark },
  })
}
