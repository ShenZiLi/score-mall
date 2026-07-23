import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getUserInfo } from '../api'

const TOKEN_KEY = 'mall_token'
const USER_KEY = 'mall_user'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem(TOKEN_KEY) || '')
  const userInfo = ref(
    JSON.parse(localStorage.getItem(USER_KEY) || 'null') || null,
  )

  const isLogin = computed(() => !!token.value)
  const points = computed(() => (userInfo.value ? userInfo.value.points : 0))

  function setToken(t) {
    token.value = t
    if (t) localStorage.setItem(TOKEN_KEY, t)
    else localStorage.removeItem(TOKEN_KEY)
  }

  function setUserInfo(info) {
    userInfo.value = info
    if (info) localStorage.setItem(USER_KEY, JSON.stringify(info))
    else localStorage.removeItem(USER_KEY)
  }

  async function fetchUserInfo() {
    const info = await getUserInfo()
    setUserInfo(info)
    return info
  }

  function clear() {
    setToken('')
    setUserInfo(null)
  }

  return {
    token,
    userInfo,
    isLogin,
    points,
    setToken,
    setUserInfo,
    fetchUserInfo,
    clear,
  }
})
