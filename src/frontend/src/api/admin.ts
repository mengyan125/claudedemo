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

/* ===== 管理员反馈查看 ===== */

/* 管理员反馈列表项 */
export interface AdminFeedbackItem {
  id: number
  title: string
  content: string
  categoryName: string
  gradeName: string
  className: string
  teacherName: string | null
  studentName: string
  isAnonymous: boolean
  status: 'submitted' | 'replied'
  hasUnread: boolean
  isFavorited: boolean
  createTime: string
}

/* 管理员反馈列表查询参数 */
export interface AdminFeedbackListParams {
  categoryId?: number
  status?: string
  keyword?: string
  pageNum: number
  pageSize: number
}

/* 分页结果 */
export interface PageResult<T> {
  list: T[]
  total: number
  pageNum: number
  pageSize: number
}

/* 管理员反馈详情 */
export interface AdminFeedbackDetail {
  id: number
  title: string
  content: string
  categoryId: number
  categoryName: string
  teacherId: number | null
  teacherName: string | null
  gradeName: string
  className: string
  studentName: string
  isAnonymous: boolean
  isTeachingRelated: boolean
  status: 'submitted' | 'replied'
  attachments: AdminAttachment[]
  replies: AdminReply[]
  createTime: string
}

export interface AdminAttachment {
  id: number
  fileName: string
  fileUrl: string
  fileType: string
}

export interface AdminReply {
  id: number
  content: string
  replyUserName: string
  replyUserType: 'admin' | 'student'
  createTime: string
}

/* 反馈统计数据 */
export interface FeedbackStatistics {
  totalCount: number
  semesterCount: number
  monthCount: number
  categoryDistribution: Array<{ name: string; count: number; percentage: number }>
  semesterTrend: Array<{ semester: string; count: number }>
  teacherTop10: Array<{ rank: number; teacherName: string; subject: string; gradeName: string; className: string; count: number }>
}

/* 教师被反馈列表项 */
export interface TeacherFeedbackItem {
  id: number
  title: string
  categoryName: string
  gradeName: string
  className: string
  teacherName: string
  studentName: string
  isAnonymous: boolean
  hasUnread: boolean
  isFavorited: boolean
  createTime: string
}

/* 教师信息 */
export interface TeacherInfo {
  id: number
  name: string
  subject: string
  totalCount: number
}

/* 获取管理员反馈列表 */
export function getAdminFeedbackListApi(params: AdminFeedbackListParams) {
  return request.get<ApiResponse<PageResult<AdminFeedbackItem>>>('/admin/feedback/list', { params })
}

/* 获取管理员反馈详情 */
export function getAdminFeedbackDetailApi(id: number) {
  return request.get<ApiResponse<AdminFeedbackDetail>>(`/admin/feedback/${id}`)
}

/* 管理员回复反馈 */
export function adminReplyFeedbackApi(feedbackId: number, content: string) {
  return request.post<ApiResponse<null>>(`/admin/feedback/${feedbackId}/reply`, { content })
}

/* 收藏/取消收藏 */
export function toggleFavoriteApi(feedbackId: number) {
  return request.post<ApiResponse<null>>(`/admin/feedback/${feedbackId}/favorite`)
}

/* 获取反馈统计 */
export function getStatisticsApi() {
  return request.get<ApiResponse<FeedbackStatistics>>('/admin/statistics')
}

/* 获取教师被反馈列表 */
export function getTeacherFeedbackListApi(teacherId: number) {
  return request.get<ApiResponse<{ teacher: TeacherInfo; list: TeacherFeedbackItem[] }>>(`/admin/statistics/teacher/${teacherId}`)
}

/* 获取快捷回复列表（用于回复弹窗） */
export function getQuickRepliesForReplyApi() {
  return request.get<ApiResponse<Array<{ id: number; content: string }>>>('/admin/quick-reply/list')
}
