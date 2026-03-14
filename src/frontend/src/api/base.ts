import request from '@/utils/request'
import type { ApiResponse } from '@/utils/request'

/* 学期 */
export interface SemesterItem {
  id: number
  semesterName: string
  startDate: string
  endDate: string
  isCurrent: boolean
}

/* 年级 */
export interface GradeItem {
  id: number
  gradeName: string
  sortOrder: number
}

/* 班级 */
export interface ClassItem {
  id: number
  className: string
  gradeId: number
  gradeName?: string
  sortOrder: number
}

/* 学期列表 */
export function getSemesterListApi() {
  return request.get<ApiResponse<SemesterItem[]>>('/base/semester/list')
}

/* 当前学期 */
export function getCurrentSemesterApi() {
  return request.get<ApiResponse<SemesterItem>>('/base/semester/current')
}

/* 创建学期 */
export function createSemesterApi(data: Omit<SemesterItem, 'id' | 'isCurrent'>) {
  return request.post<ApiResponse<null>>('/base/semester', data)
}

/* 更新学期 */
export function updateSemesterApi(id: number, data: Omit<SemesterItem, 'id' | 'isCurrent'>) {
  return request.put<ApiResponse<null>>(`/base/semester/${id}`, data)
}

/* 设置当前学期 */
export function setCurrentSemesterApi(id: number) {
  return request.put<ApiResponse<null>>(`/base/semester/${id}/current`)
}

/* 删除学期 */
export function deleteSemesterApi(id: number) {
  return request.delete<ApiResponse<null>>(`/base/semester/${id}`)
}

/* 年级列表 */
export function getGradeListApi() {
  return request.get<ApiResponse<GradeItem[]>>('/base/grade/list')
}

/* 创建年级 */
export function createGradeApi(data: Omit<GradeItem, 'id'>) {
  return request.post<ApiResponse<null>>('/base/grade', data)
}

/* 更新年级 */
export function updateGradeApi(id: number, data: Partial<GradeItem>) {
  return request.put<ApiResponse<null>>(`/base/grade/${id}`, data)
}

/* 删除年级 */
export function deleteGradeApi(id: number) {
  return request.delete<ApiResponse<null>>(`/base/grade/${id}`)
}

/* 班级列表 */
export function getClassListApi(gradeId?: number) {
  return request.get<ApiResponse<ClassItem[]>>('/base/class/list', {
    params: gradeId ? { gradeId } : {}
  })
}

/* 创建班级 */
export function createClassApi(data: Omit<ClassItem, 'id' | 'gradeName'>) {
  return request.post<ApiResponse<null>>('/base/class', data)
}

/* 更新班级 */
export function updateClassApi(id: number, data: Partial<ClassItem>) {
  return request.put<ApiResponse<null>>(`/base/class/${id}`, data)
}

/* 删除班级 */
export function deleteClassApi(id: number) {
  return request.delete<ApiResponse<null>>(`/base/class/${id}`)
}

/* 师生关系 - 学生分班 */
export interface StudentAssignment {
  id: number
  studentId: number
  studentName: string
  username: string
  classId: number
  className?: string
  assignTime: string
}

/* 师生关系 - 教师任课 */
export interface TeacherAssignment {
  id: number
  teacherId: number
  teacherName: string
  username: string
  classId: number
  className?: string
  subject: string
  assignTime: string
}

/* 获取班级学生列表 */
export function getClassStudentsApi(classId: number) {
  return request.get<ApiResponse<StudentAssignment[]>>(`/base/relation/class/${classId}/students`)
}

/* 获取未分配学生列表 */
export function getUnassignedStudentsApi() {
  return request.get<ApiResponse<StudentAssignment[]>>('/base/relation/students/unassigned')
}

/* 分配学生到班级 */
export function assignStudentsApi(classId: number, studentIds: number[]) {
  return request.post<ApiResponse<null>>(`/base/relation/class/${classId}/students`, { studentIds })
}

/* 移除学生分配 */
export function removeStudentAssignmentApi(id: number) {
  return request.delete<ApiResponse<null>>(`/base/relation/student-assignment/${id}`)
}

/* 获取班级教师列表 */
export function getClassTeachersApi(classId: number) {
  return request.get<ApiResponse<TeacherAssignment[]>>(`/base/relation/class/${classId}/teachers`)
}

/* 分配教师到班级 */
export function assignTeacherApi(classId: number, data: { teacherId: number; subject: string }) {
  return request.post<ApiResponse<null>>(`/base/relation/class/${classId}/teachers`, data)
}

/* 移除教师任课 */
export function removeTeacherAssignmentApi(id: number) {
  return request.delete<ApiResponse<null>>(`/base/relation/teacher-assignment/${id}`)
}
