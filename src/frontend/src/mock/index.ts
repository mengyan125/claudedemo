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

const MOCK_CATEGORIES = [
  { id: 1, name: '教学质量', isTeachingRelated: true, sortOrder: 1, status: 1, feedbackCount: 12 },
  { id: 2, name: '校园环境', isTeachingRelated: false, sortOrder: 2, status: 1, feedbackCount: 5 },
  { id: 3, name: '后勤服务', isTeachingRelated: false, sortOrder: 3, status: 0, feedbackCount: 0 },
  { id: 4, name: '课程安排', isTeachingRelated: true, sortOrder: 4, status: 1, feedbackCount: 0 }
]

const MOCK_STUDENT = {
  id: 4,
  username: 'student01',
  realName: '王子学生',
  userType: 'student' as const,
  roles: ['学生'],
  schoolName: '101中学'
}

const MOCK_STUDENT_CATEGORIES: Array<{ id: number; name: string; isTeachingRelated: boolean }> = [
  { id: 1, name: '教学', isTeachingRelated: true },
  { id: 2, name: '后勤', isTeachingRelated: false },
  { id: 3, name: '学校食堂', isTeachingRelated: false },
  { id: 4, name: '校园安全', isTeachingRelated: false }
]

const MOCK_TEACHERS = [
  { id: 1, name: '李学', subject: '英语' },
  { id: 2, name: '王老师', subject: '数学' },
  { id: 3, name: '张老师', subject: '语文' }
]

const MOCK_FEEDBACKS = [
  {
    id: 1, title: '英语老师上课太快', content: '英语老师讲课速度太快，有点跟不上，希望上课的时候慢一点。希望英语老师能多带我们练习口语，现在做题太多，没有进行口语练习，提升我们的口语水平。',
    categoryId: 1, categoryName: '教学', teacherId: 1, teacherName: '英语—李学', isTeachingRelated: true, isAnonymous: false,
    gradeName: '高一', className: '高一（3班）', studentName: '王子', isFavorited: false,
    status: 'replied' as const, hasUnread: true, createTime: '2025-03-21 10:40:25',
    attachments: [{ id: 1, fileName: '实录.jpg', fileUrl: '/mock/files/record.jpg', fileType: 'image/jpeg' }],
    replies: [
      { id: 1, content: '同学你好，你说的问题我们非常重视，想再和你确认下信息，是老师说话语速太快，还是授课的内容太快？', replyUserName: '管理员', replyUserType: 'admin' as const, createTime: '2025-03-21 16:48:45' }
    ]
  },
  {
    id: 2, title: '公共区域卫生容易被破坏', content: '学校公共区域卫生刚搞完就被破坏，希望建立卫生巡查队，加强对公共区域的管理。',
    categoryId: 2, categoryName: '后勤', teacherId: null, teacherName: null, isTeachingRelated: false, isAnonymous: true,
    gradeName: '高一', className: '高一（3班）', studentName: '匿名', isFavorited: false,
    status: 'submitted' as const, hasUnread: false, createTime: '2025-03-20 14:20:00',
    attachments: [], replies: []
  },
  {
    id: 3, title: '食堂饭菜口味', content: '学校食堂的饭菜味道有待提高，能不能做点辣的，味道重的，现在的菜太清淡了。',
    categoryId: 3, categoryName: '学校食堂', teacherId: null, teacherName: null, isTeachingRelated: false, isAnonymous: false,
    gradeName: '高二', className: '高二（1班）', studentName: '张三', isFavorited: true,
    status: 'replied' as const, hasUnread: true, createTime: '2025-03-19 09:15:00',
    attachments: [],
    replies: [
      { id: 2, content: '同学你好，你的建议已经转达给食堂管理处，他们会适当调整菜品口味。', replyUserName: '管理员', replyUserType: 'admin' as const, createTime: '2025-03-19 15:30:00' }
    ]
  },
  {
    id: 4, title: '卫生问题', content: '学校公共区域卫生刚搞完就被破坏，希望建立卫生巡查队。',
    categoryId: 2, categoryName: '后勤', teacherId: null, teacherName: null, isTeachingRelated: false, isAnonymous: false,
    gradeName: '高一', className: '高一（3班）', studentName: '李四', isFavorited: false,
    status: 'submitted' as const, hasUnread: false, createTime: '2025-03-19 08:00:00',
    attachments: [], replies: []
  },
  {
    id: 5, title: '教学方面的反馈意见', content: '英语老师讲课速度太快，有点跟不上，希望上课的时候慢一点。',
    categoryId: 1, categoryName: '教学', teacherId: 1, teacherName: '英语—李学', isTeachingRelated: true, isAnonymous: false,
    gradeName: '高一', className: '高一（3班）', studentName: '匿名', isFavorited: false,
    status: 'submitted' as const, hasUnread: false, createTime: '2025-03-18 16:00:00',
    attachments: [], replies: []
  },
  {
    id: 6, title: '暂存的反馈', content: '这是一条暂存的反馈内容，还没有正式提交。',
    categoryId: 1, categoryName: '教学', teacherId: 2, teacherName: '数学—王老师', isTeachingRelated: true, isAnonymous: false,
    gradeName: '高一', className: '高一（3班）', studentName: '王子', isFavorited: false,
    status: 'draft' as const, hasUnread: false, createTime: '2025-03-17 10:00:00',
    attachments: [], replies: []
  }
]

const MOCK_QUICK_REPLIES = [
  { id: 1, content: '感谢你的反馈，我们会认真处理并尽快给你回复。', sortOrder: 1, createUserName: '系统管理员', createTime: '2026-01-15 10:00:00' },
  { id: 2, content: '你的问题已经转交相关部门处理，请耐心等待。', sortOrder: 2, createUserName: '系统管理员', createTime: '2026-01-20 14:30:00' },
  { id: 3, content: '该问题已经解决，如有其他疑问请继续反馈。', sortOrder: 3, createUserName: '系统管理员', createTime: '2026-02-10 09:00:00' }
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
      if (body.username === 'student' && body.password === '123456') {
        return ok({ token: MOCK_TOKEN, userInfo: MOCK_STUDENT })
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

  /* 类别列表 */
  { method: 'get', pattern: /\/admin\/category\/list$/, handler: () => ok(MOCK_CATEGORIES) },

  /* 快捷回复列表 */
  { method: 'get', pattern: /\/admin\/quick-reply\/list$/, handler: () => ok(MOCK_QUICK_REPLIES) },

  /* ===== 管理员反馈查看 ===== */

  /* 管理员反馈列表 */
  {
    method: 'get',
    pattern: /\/admin\/feedback\/list/,
    handler: (config) => {
      const params = config.params || {}
      const categoryId = params.categoryId ? Number(params.categoryId) : 0
      const status = params.status || ''
      const keyword = (params.keyword || '').toLowerCase()
      const pageNum = params.pageNum || 1
      const pageSize = params.pageSize || 10

      let list = MOCK_FEEDBACKS.filter(f => f.status !== 'draft')
      if (categoryId) list = list.filter(f => f.categoryId === categoryId)
      if (status) list = list.filter(f => f.status === status)
      if (keyword) list = list.filter(f => f.title.includes(keyword) || f.content.includes(keyword))

      const total = list.length
      const start = (pageNum - 1) * pageSize
      const pageList = list.slice(start, start + pageSize).map(f => ({
        id: f.id, title: f.title, content: f.content.slice(0, 30) + '……',
        categoryName: f.categoryName, gradeName: (f as any).gradeName || '高一',
        className: (f as any).className || '高一（3班）',
        teacherName: f.teacherName,
        studentName: f.isAnonymous ? '匿名' : ((f as any).studentName || '学生'),
        isAnonymous: f.isAnonymous,
        status: f.status, hasUnread: f.hasUnread,
        isFavorited: (f as any).isFavorited || false, createTime: f.createTime
      }))
      return ok({ list: pageList, total, pageNum, pageSize })
    }
  },

  /* 管理员反馈详情 */
  {
    method: 'get',
    pattern: /\/admin\/feedback\/(\d+)$/,
    handler: (config) => {
      const match = config.url?.match(/\/admin\/feedback\/(\d+)$/)
      const id = match ? Number(match[1]) : 0
      const item = MOCK_FEEDBACKS.find(f => f.id === id)
      if (!item) return { code: 404, message: '反馈不存在', data: null }
      return ok({
        ...item,
        gradeName: (item as any).gradeName || '高一',
        className: (item as any).className || '高一（3班）',
        studentName: item.isAnonymous ? '匿名' : ((item as any).studentName || '学生'),
        isFavorited: (item as any).isFavorited || false
      })
    }
  },

  /* 管理员回复 */
  {
    method: 'post',
    pattern: /\/admin\/feedback\/\d+\/reply$/,
    handler: () => ok(null)
  },

  /* 收藏/取消收藏 */
  {
    method: 'post',
    pattern: /\/admin\/feedback\/\d+\/favorite$/,
    handler: () => ok(null)
  },

  /* 反馈统计 */
  {
    method: 'get',
    pattern: /\/admin\/statistics$/,
    handler: () => ok({
      totalCount: 100,
      semesterCount: 45,
      monthCount: 3,
      categoryDistribution: [
        { name: '教学', count: 50, percentage: 50 },
        { name: '后勤', count: 20, percentage: 20 },
        { name: '学校食堂', count: 18, percentage: 18 },
        { name: '校园安全', count: 12, percentage: 12 }
      ],
      semesterTrend: [
        { semester: '2024年第一学期', count: 28 },
        { semester: '2024年第二学期', count: 35 },
        { semester: '2025年第一学期', count: 42 },
        { semester: '2025年第二学期', count: 45 }
      ],
      teacherTop10: [
        { rank: 1, teacherName: '李学', subject: '英语', gradeName: '高一', className: '高一（3班）', count: 20 },
        { rank: 2, teacherName: '王老师', subject: '数学', gradeName: '高一', className: '高一（1班）', count: 15 },
        { rank: 3, teacherName: '张老师', subject: '语文', gradeName: '高二', className: '高二（2班）', count: 12 },
        { rank: 4, teacherName: '刘老师', subject: '物理', gradeName: '高一', className: '高一（2班）', count: 10 },
        { rank: 5, teacherName: '陈老师', subject: '化学', gradeName: '高二', className: '高二（1班）', count: 8 },
        { rank: 6, teacherName: '赵老师', subject: '生物', gradeName: '高一', className: '高一（3班）', count: 7 },
        { rank: 7, teacherName: '孙老师', subject: '历史', gradeName: '高二', className: '高二（2班）', count: 5 },
        { rank: 8, teacherName: '周老师', subject: '地理', gradeName: '高一', className: '高一（1班）', count: 4 },
        { rank: 9, teacherName: '吴老师', subject: '政治', gradeName: '高二', className: '高二（1班）', count: 3 },
        { rank: 10, teacherName: '郑老师', subject: '体育', gradeName: '高一', className: '高一（2班）', count: 2 }
      ]
    })
  },

  /* 教师被反馈列表 */
  {
    method: 'get',
    pattern: /\/admin\/statistics\/teacher\/(\d+)$/,
    handler: (config) => {
      const match = config.url?.match(/\/teacher\/(\d+)$/)
      const teacherId = match ? Number(match[1]) : 0
      const teacherMap: Record<number, { name: string; subject: string }> = {
        1: { name: '李学', subject: '英语' },
        2: { name: '王老师', subject: '数学' },
        3: { name: '张老师', subject: '语文' }
      }
      const teacher = teacherMap[teacherId] || { name: '未知', subject: '' }
      const list = MOCK_FEEDBACKS
        .filter(f => f.teacherId === teacherId && f.status !== 'draft')
        .map(f => ({
          id: f.id, title: f.title,
          categoryName: f.categoryName,
          gradeName: (f as any).gradeName || '高一',
          className: (f as any).className || '高一（3班）',
          teacherName: f.teacherName || '',
          studentName: f.isAnonymous ? '匿名' : ((f as any).studentName || '学生'),
          isAnonymous: f.isAnonymous,
          hasUnread: f.hasUnread,
          isFavorited: (f as any).isFavorited || false,
          createTime: f.createTime
        }))
      return ok({
        teacher: { id: teacherId, name: teacher.name, subject: teacher.subject, totalCount: list.length },
        list
      })
    }
  },

  /* ===== 学生端 ===== */

  /* 学生可见类别 */
  { method: 'get', pattern: /\/student\/categories$/, handler: () => ok(MOCK_STUDENT_CATEGORIES) },

  /* 教师列表 */
  { method: 'get', pattern: /\/student\/teachers$/, handler: () => ok(MOCK_TEACHERS) },

  /* 反馈列表 */
  {
    method: 'get',
    pattern: /\/student\/feedback\/list/,
    handler: (config) => {
      const params = config.params || {}
      const categoryId = params.categoryId ? Number(params.categoryId) : 0
      const keyword = (params.keyword || '').toLowerCase()
      const pageNum = params.pageNum || 1
      const pageSize = params.pageSize || 10

      let list = MOCK_FEEDBACKS
      if (categoryId) list = list.filter(f => f.categoryId === categoryId)
      if (keyword) list = list.filter(f => f.title.includes(keyword) || f.content.includes(keyword))

      const total = list.length
      const start = (pageNum - 1) * pageSize
      const pageList = list.slice(start, start + pageSize).map(f => ({
        id: f.id, title: f.title, content: f.content.slice(0, 30) + '……',
        categoryId: f.categoryId, categoryName: f.categoryName,
        teacherName: f.teacherName, isAnonymous: f.isAnonymous,
        status: f.status, hasUnread: f.hasUnread, createTime: f.createTime
      }))
      return ok({ list: pageList, total, pageNum, pageSize })
    }
  },

  /* 反馈详情 */
  {
    method: 'get',
    pattern: /\/student\/feedback\/(\d+)$/,
    handler: (config) => {
      const match = config.url?.match(/\/student\/feedback\/(\d+)$/)
      const id = match ? Number(match[1]) : 0
      const item = MOCK_FEEDBACKS.find(f => f.id === id)
      if (!item) return { code: 404, message: '反馈不存在', data: null }
      return ok(item)
    }
  },

  /* 上传文件 */
  {
    method: 'post',
    pattern: /\/upload\/file$/,
    handler: () => ok({ id: Date.now(), fileName: '上传文件.jpg', fileUrl: '/mock/files/upload.jpg' })
  },

  /* 追加回复 */
  {
    method: 'post',
    pattern: /\/student\/feedback\/\d+\/reply$/,
    handler: () => ok(null)
  },

  /* 提交/暂存反馈 */
  {
    method: 'post',
    pattern: /\/student\/feedback$/,
    handler: () => ok({ id: Date.now() })
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

  console.log('[Mock] 开发环境 Mock 已启用，管理员：admin / 123456，学生：student / 123456')
}
