import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

/* 用户角色类型 */
export type UserType = 'student' | 'teacher' | 'admin'

/* 用户信息接口 */
export interface UserInfo {
  id: number
  username: string
  realName: string
  userType: UserType
  roles: string[]
  schoolName?: string
}

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem('token') || '')
  const userInfo = ref<UserInfo | null>(null)

  const isLoggedIn = computed(() => !!token.value)
  const isStudent = computed(() => userInfo.value?.userType === 'student')
  const isAdmin = computed(() =>
    userInfo.value?.roles?.some(r =>
      ['SYSTEM_ADMIN', 'ROLE_ADMIN', 'CATEGORY_ADMIN'].includes(r)
    ) ?? false
  )

  /* 设置 token */
  function setToken(newToken: string) {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  /* 设置用户信息 */
  function setUserInfo(info: UserInfo) {
    userInfo.value = info
  }

  /* 退出登录 */
  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    isStudent,
    isAdmin,
    setToken,
    setUserInfo,
    logout
  }
})
