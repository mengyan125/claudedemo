import request from '@/utils/request'
import type { ApiResponse } from '@/utils/request'

/* 学生可见的类别（仅启用状态） */
export interface StudentCategory {
  id: number
  name: string
  isTeachingRelated: boolean
}

/* 教师选项 */
export interface TeacherOption {
  id: number
  name: string
  subject: string
}

/* 反馈列表项 */
export interface FeedbackListItem {
  id: number
  title: string
  content: string
  categoryId: number
  categoryName: string
  teacherName: string | null
  isAnonymous: boolean
  status: 'draft' | 'submitted' | 'replied'
  hasUnread: boolean
  createTime: string
}

/* 反馈详情 */
export interface FeedbackDetail {
  id: number
  title: string
  content: string
  categoryId: number
  categoryName: string
  teacherId: number | null
  teacherName: string | null
  isTeachingRelated: boolean
  isAnonymous: boolean
  status: 'draft' | 'submitted' | 'replied'
  attachments: AttachmentItem[]
  replies: ReplyItem[]
  createTime: string
}

/* 附件 */
export interface AttachmentItem {
  id: number
  fileName: string
  fileUrl: string
  fileType: string
}

/* 回复记录 */
export interface ReplyItem {
  id: number
  content: string
  replyUserName: string
  userType: 'admin' | 'student'
  createTime: string
}

/* 提交/暂存反馈参数 */
export interface FeedbackSubmitParams {
  id?: number
  categoryId: number
  isTeachingRelated: boolean
  teacherId: number | null
  title: string
  content: string
  isAnonymous: boolean
  status: 'draft' | 'submitted'
  attachmentIds: number[]
}

/* 反馈列表查询参数 */
export interface FeedbackListParams {
  categoryId?: number
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

/* 获取学生可见类别 */
export function getStudentCategoriesApi() {
  return request.get<ApiResponse<StudentCategory[]>>('/student/categories')
}

/* 获取教师列表 */
export function getStudentTeachersApi() {
  return request.get<ApiResponse<TeacherOption[]>>('/student/teachers')
}

/* 提交/暂存反馈 */
export function submitFeedbackApi(data: FeedbackSubmitParams) {
  return request.post<ApiResponse<{ id: number }>>('/student/feedback', data)
}

/* 获取反馈列表 */
export function getFeedbackListApi(params: FeedbackListParams) {
  return request.get<ApiResponse<PageResult<FeedbackListItem>>>('/student/feedback/list', { params })
}

/* 获取反馈详情 */
export function getFeedbackDetailApi(id: number) {
  return request.get<ApiResponse<FeedbackDetail>>(`/student/feedback/${id}`)
}

/* 追加回复 */
export function addReplyApi(feedbackId: number, content: string) {
  return request.post<ApiResponse<null>>(`/student/feedback/${feedbackId}/reply`, { content })
}

/* 上传附件 */
export function uploadFileApi(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<ApiResponse<{ id: number; fileName: string; fileUrl: string }>>('/upload/file', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
