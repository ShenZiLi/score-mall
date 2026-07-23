import request from '../utils/request'

export function listPointsLogs(params) {
  return request.get('/admin/points/logs', { params })
}
