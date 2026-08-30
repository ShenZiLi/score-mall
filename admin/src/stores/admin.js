import { defineStore } from 'pinia'

const TOKEN_KEY = 'admin_token'
const ADMIN_INFO_KEY = 'admin_info'

export const useAdminStore = defineStore('admin', {
  state: () => ({
    token: localStorage.getItem(TOKEN_KEY) || '',
    adminInfo: JSON.parse(localStorage.getItem(ADMIN_INFO_KEY) || 'null'),
  }),
  getters: {
    isLogin: (state) => !!state.token,
    nickname: (state) => state.adminInfo?.nickname || state.adminInfo?.username || '管理员',
    avatar: (state) => state.adminInfo?.avatar || '',
    role: (state) => state.adminInfo?.role || '',
  },
  actions: {
    setAuth(token, adminInfo) {
      this.token = token
      this.adminInfo = adminInfo
      localStorage.setItem(TOKEN_KEY, token)
      localStorage.setItem(ADMIN_INFO_KEY, JSON.stringify(adminInfo))
    },
    setAdminInfo(adminInfo) {
      this.adminInfo = adminInfo
      localStorage.setItem(ADMIN_INFO_KEY, JSON.stringify(adminInfo))
    },
    clearAuth() {
      this.token = ''
      this.adminInfo = null
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem(ADMIN_INFO_KEY)
    },
  },
})
