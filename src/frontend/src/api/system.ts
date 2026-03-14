import request from '@/utils/request'
import type { ApiResponse } from '@/utils/request'

/* 用户信息 */
export interface UserItem {
  id: number
  username: string
  realName: string
  userType: 'student' | 'teacher' | 'admin'
  roles: string[]
  status: number
  createTime: string
}

/* 用户列表请求参数 */
export interface UserListParams {
  keyword?: string
  pageNum: number
  pageSize: number
}

/* 分页响应 */
export interface PageResult<T> {
  list: T[]
  total: number
  pageNum: number
  pageSize: number
}

/* 创建用户参数 */
export interface CreateUserParams {
  username: string
  realName: string
  password: string
  userType: 'student' | 'teacher'
}

/* 用户列表 */
export function getUserListApi(params: UserListParams) {
  return request.get<ApiResponse<PageResult<UserItem>>>('/system/user/list', { params })
}

/* 创建用户 */
export function createUserApi(data: CreateUserParams) {
  return request.post<ApiResponse<null>>('/system/user', data)
}

/* 批量创建用户 */
export function batchCreateUserApi(data: CreateUserParams[]) {
  return request.post<ApiResponse<null>>('/system/user/batch', data)
}

/* 更新用户 */
export function updateUserApi(id: number, data: Partial<CreateUserParams>) {
  return request.put<ApiResponse<null>>(`/system/user/${id}`, data)
}

/* 删除用户 */
export function deleteUserApi(id: number) {
  return request.delete<ApiResponse<null>>(`/system/user/${id}`)
}

/* 启用/禁用用户 */
export function toggleUserStatusApi(id: number, status: number) {
  return request.put<ApiResponse<null>>(`/system/user/${id}/status`, { status })
}
