/**
 * 开发环境 Mock 数据
 * 通过拦截 Axios 适配器实现，不依赖额外库
 * 仅在 import.meta.env.DEV 时启用
 */
import type { InternalAxiosRequestConfig } from 'axios'
import request from '@/utils/request'

/* Mock 数据 */
const MOCK_TOKEN = 'mock-jwt-token-dev-2026'

const MOCK_ADMIN = {
  id: 1,
  username: 'admin',
  realName: '系统管理员',
  userType: 'admin' as const,
  roles: ['SYSTEM_ADMIN'],
  schoolName: '示范学校'
}

const MOCK_USERS = [
  { id: 1, username: 'admin', realName: '系统管理员', userType: 'admin', roles: ['SYSTEM_ADMIN'], status: 1, createTime: '2026-01-01 08:00:00' },
  { id: 2, username: 'teacher01', realName: '王老师', userType: 'teacher', roles: ['教师'], status: 1, createTime: '2026-02-15 09:00:00' },
  { id: 3, username: 'teacher02', realName: '李老师', userType: 'teacher', roles: ['教师'], status: 1, createTime: '2026-02-15 09:00:00' },
  { id: 4, username: 'student01', realName: '张三', userType: 'student', roles: ['学生'], status: 1, createTime: '2026-03-01 10:00:00' },
  { id: 5, username: 'student02', realName: '李四', userType: 'student', roles: ['学生'], status: 1, createTime: '2026-03-01 10:00:00' },
  { id: 6, username: 'student03', realName: '王五', userType: 'student', roles: ['学生'], status: 0, createTime: '2026-03-02 10:00:00' },
  { id: 7, username: 'student04', realName: '赵六', userType: 'student', roles: ['学生'], status: 1, createTime: '2026-03-02 10:00:00' },
  { id: 8, username: 'student05', realName: '周七', userType: 'student', roles: ['学生'], status: 1, createTime: '2026-03-03 10:00:00' }
]

const MOCK_SEMESTERS = [
  { id: 1, semesterName: '2025-2026学年第一学期', startDate: '2025-09-01', endDate: '2026-01-15', isCurrent: false },
  { id: 2, semesterName: '2025-2026学年第二学期', startDate: '2026-02-17', endDate: '2026-07-05', isCurrent: true }
]

const MOCK_GRADES = [
  { id: 1, gradeName: '一年级', sortOrder: 1 },
  { id: 2, gradeName: '二年级', sortOrder: 2 },
  { id: 3, gradeName: '三年级', sortOrder: 3 }
]

const MOCK_CLASSES = [
  { id: 1, className: '一班', gradeId: 1, gradeName: '一年级', sortOrder: 1 },
  { id: 2, className: '二班', gradeId: 1, gradeName: '一年级', sortOrder: 2 },
  { id: 3, className: '一班', gradeId: 2, gradeName: '二年级', sortOrder: 1 },
  { id: 4, className: '二班', gradeId: 2, gradeName: '二年级', sortOrder: 2 },
  { id: 5, className: '一班', gradeId: 3, gradeName: '三年级', sortOrder: 1 },
  { id: 6, className: '二班', gradeId: 3, gradeName: '三年级', sortOrder: 2 }
]

const MOCK_STUDENT_ASSIGNMENTS = [
  { id: 1, studentId: 4, studentName: '张三', username: 'student01', classId: 5, className: '三年级一班', assignTime: '2026-03-01' },
  { id: 2, studentId: 5, studentName: '李四', username: 'student02', classId: 5, className: '三年级一班', assignTime: '2026-03-01' },
  { id: 3, studentId: 7, studentName: '赵六', username: 'student04', classId: 5, className: '三年级一班', assignTime: '2026-03-02' }
]

const MOCK_TEACHER_ASSIGNMENTS = [
  { id: 1, teacherId: 2, teacherName: '王老师', username: 'teacher01', classId: 5, className: '三年级一班', subject: '数学', assignTime: '2026-02-20' },
  { id: 2, teacherId: 3, teacherName: '李老师', username: 'teacher02', classId: 5, className: '三年级一班', subject: '语文', assignTime: '2026-02-20' }
]

/* 成功响应包装 */
function ok<T>(data: T) {
  return { code: 200, message: 'success', data }
}

/* 路由匹配与处理 */
type MockHandler = (config: InternalAxiosRequestConfig) => unknown

interface MockRoute {
  method: string
  pattern: RegExp
  handler: MockHandler
}

const routes: MockRoute[] = [
  /* 登录 */
  {
    method: 'post',
    pattern: /\/auth\/login$/,
    handler: (config) => {
      const body = typeof config.data === 'string' ? JSON.parse(config.data) : (config.data || {})
      if (body.username === 'admin' && body.password === '123456') {
        return ok({ token: MOCK_TOKEN, userInfo: MOCK_ADMIN })
      }
      return { code: 401, message: '用户名或密码错误', data: null }
    }
  },
  /* 登出 */
  { method: 'post', pattern: /\/auth\/logout$/, handler: () => ok(null) },

  /* 用户列表 */
  {
    method: 'get',
    pattern: /\/system\/user\/list/,
    handler: (config) => {
      const params = config.params || {}
      const keyword = (params.keyword || '').toLowerCase()
      const pageNum = params.pageNum || 1
      const pageSize = params.pageSize || 10
      let list = MOCK_USERS
      if (keyword) {
        list = list.filter(u => u.username.includes(keyword) || u.realName.includes(keyword))
      }
      const start = (pageNum - 1) * pageSize
      return ok({ list: list.slice(start, start + pageSize), total: list.length, pageNum, pageSize })
    }
  },

  /* 学期列表 */
  { method: 'get', pattern: /\/base\/semester\/list$/, handler: () => ok(MOCK_SEMESTERS) },
  { method: 'get', pattern: /\/base\/semester\/current$/, handler: () => ok(MOCK_SEMESTERS.find(s => s.isCurrent)) },

  /* 年级列表 */
  { method: 'get', pattern: /\/base\/grade\/list$/, handler: () => ok(MOCK_GRADES) },

  /* 班级列表 */
  {
    method: 'get',
    pattern: /\/base\/class\/list/,
    handler: (config) => {
      const gradeId = config.params?.gradeId
      const list = gradeId ? MOCK_CLASSES.filter(c => c.gradeId === gradeId) : MOCK_CLASSES
      return ok(list)
    }
  },

  /* 师生关系 - 班级学生 */
  {
    method: 'get',
    pattern: /\/base\/relation\/class\/(\d+)\/students$/,
    handler: (config) => {
      const match = config.url?.match(/\/class\/(\d+)\/students/)
      const classId = match ? Number(match[1]) : 0
      return ok(MOCK_STUDENT_ASSIGNMENTS.filter(s => s.classId === classId))
    }
  },

  /* 未分配学生 */
  {
    method: 'get',
    pattern: /\/base\/relation\/students\/unassigned$/,
    handler: () => {
      const assignedIds = MOCK_STUDENT_ASSIGNMENTS.map(s => s.studentId)
      const unassigned = MOCK_USERS
        .filter(u => u.userType === 'student' && !assignedIds.includes(u.id))
        .map(u => ({ id: 0, studentId: u.id, studentName: u.realName, username: u.username, classId: 0, assignTime: '' }))
      return ok(unassigned)
    }
  },

  /* 师生关系 - 班级教师 */
  {
    method: 'get',
    pattern: /\/base\/relation\/class\/(\d+)\/teachers$/,
    handler: (config) => {
      const match = config.url?.match(/\/class\/(\d+)\/teachers/)
      const classId = match ? Number(match[1]) : 0
      return ok(MOCK_TEACHER_ASSIGNMENTS.filter(t => t.classId === classId))
    }
  },

  /* 通用写操作（POST/PUT/DELETE）返回成功 */
  { method: 'post', pattern: /.*/, handler: () => ok(null) },
  { method: 'put', pattern: /.*/, handler: () => ok(null) },
  { method: 'delete', pattern: /.*/, handler: () => ok(null) }
]

/* 安装 Mock 拦截器 */
export function setupMock() {
  request.interceptors.request.use((config) => {
    const method = (config.method || 'get').toLowerCase()
    const url = config.url || ''

    for (const route of routes) {
      if (route.method === method && route.pattern.test(url)) {
        const responseData = route.handler(config)
        /* 通过自定义适配器返回模拟响应 */
        config.adapter = () =>
          Promise.resolve({
            data: responseData,
            status: 200,
            statusText: 'OK',
            headers: {},
            config
          })
        break
      }
    }
    return config
  })

  console.log('[Mock] 开发环境 Mock 已启用，账号：admin / 123456')
}
