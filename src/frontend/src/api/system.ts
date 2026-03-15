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
  userType: 'student' | 'teacher' | 'role_admin'
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

/* ===== 角色管理 ===== */

/* 角色信息 */
export interface RoleItem {
  id: number
  roleCode: string
  roleName: string
}

/* 角色下的用户信息 */
export interface UserRoleItem {
  userId: number
  username: string
  realName: string
  userType: string
}

/* 获取角色列表 */
export function getRoleListApi() {
  return request.get<ApiResponse<RoleItem[]>>('/system/role/list')
}

/* 获取角色下的用户列表 */
export function getRoleUsersApi(roleId: number) {
  return request.get<ApiResponse<UserRoleItem[]>>('/system/role/users', { params: { roleId } })
}

/* 为用户分配角色 */
export function assignRoleApi(data: { userId: number; roleId: number }) {
  return request.post<ApiResponse<null>>('/system/role/assign', data)
}

/* 撤销用户角色 */
export function revokeRoleApi(data: { userId: number; roleId: number }) {
  return request.delete<ApiResponse<null>>('/system/role/revoke', { data })
}

/* ===== 类别权限配置 ===== */

/* 类别权限项 */
export interface CategoryPermissionItem {
  categoryId: number
  categoryName: string
  adminIds: number[]
}

/* 获取类别权限列表 */
export function getCategoryPermissionListApi() {
  return request.get<ApiResponse<CategoryPermissionItem[]>>('/system/category-permission/list')
}

/* 更新类别权限 */
export function updateCategoryPermissionApi(categoryId: number, adminIds: number[]) {
  return request.put<ApiResponse<null>>(`/system/category-permission/${categoryId}`, { adminIds })
}
