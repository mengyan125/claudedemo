import request from '@/utils/request'
import type { ApiResponse } from '@/utils/request'

/* 反馈类别 */
export interface CategoryItem {
  id: number
  name: string
  isTeachingRelated: boolean
  sortOrder: number
  status: number // 1=启用 0=停用
  feedbackCount: number // 该类别下反馈数量（用于判断能否编辑/删除）
}

/* 类别列表 */
export function getCategoryListApi() {
  return request.get<ApiResponse<CategoryItem[]>>('/admin/category/list')
}

/* 创建类别 */
export function createCategoryApi(data: Omit<CategoryItem, 'id' | 'feedbackCount'>) {
  return request.post<ApiResponse<null>>('/admin/category', data)
}

/* 更新类别 */
export function updateCategoryApi(id: number, data: Partial<CategoryItem>) {
  return request.put<ApiResponse<null>>(`/admin/category/${id}`, data)
}

/* 删除类别 */
export function deleteCategoryApi(id: number) {
  return request.delete<ApiResponse<null>>(`/admin/category/${id}`)
}

/* 启用/停用类别 */
export function toggleCategoryStatusApi(id: number, status: number) {
  return request.put<ApiResponse<null>>(`/admin/category/${id}/status`, { status })
}

/* 快捷回复 */
export interface QuickReplyItem {
  id: number
  content: string
  sortOrder: number
  createUserName: string
  createTime: string
}

/* 快捷回复列表 */
export function getQuickReplyListApi() {
  return request.get<ApiResponse<QuickReplyItem[]>>('/admin/quick-reply/list')
}

/* 创建快捷回复 */
export function createQuickReplyApi(data: { content: string; sortOrder: number }) {
  return request.post<ApiResponse<null>>('/admin/quick-reply', data)
}

/* 更新快捷回复 */
export function updateQuickReplyApi(id: number, data: Partial<QuickReplyItem>) {
  return request.put<ApiResponse<null>>(`/admin/quick-reply/${id}`, data)
}

/* 删除快捷回复 */
export function deleteQuickReplyApi(id: number) {
  return request.delete<ApiResponse<null>>(`/admin/quick-reply/${id}`)
}
