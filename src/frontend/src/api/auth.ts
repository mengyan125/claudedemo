import request from '@/utils/request'
import type { ApiResponse } from '@/utils/request'

/* 登录请求参数 */
export interface LoginParams {
  username: string
  password: string
}

/* 登录响应数据 */
export interface LoginResult {
  token: string
  userInfo: {
    id: number
    username: string
    realName: string
    userType: string
    roles: string[]
    schoolName?: string
  }
}

/* 登录 */
export function loginApi(data: LoginParams) {
  return request.post<ApiResponse<LoginResult>>('/auth/login', data)
}

/* 登出 */
export function logoutApi() {
  return request.post<ApiResponse<null>>('/auth/logout')
}
