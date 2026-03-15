import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getUserInfoApi } from '@/api/auth'

/* 路由配置 */
const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/pages/login/LoginPage.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/layout/MainLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      /* 学生端 */
      {
        path: 'student/feedback',
        name: 'StudentFeedback',
        component: () => import('@/pages/student/FeedbackPage.vue'),
        meta: { title: '我要反馈', requiredUserType: 'student' }
      },
      {
        path: 'student/feedback/edit/:id',
        name: 'StudentFeedbackEdit',
        component: () => import('@/pages/student/FeedbackEditPage.vue'),
        meta: { title: '编辑反馈', requiredUserType: 'student' }
      },
      {
        path: 'student/tracking',
        name: 'StudentTracking',
        component: () => import('@/pages/student/TrackingPage.vue'),
        meta: { title: '反馈跟踪', requiredUserType: 'student' }
      },
      {
        path: 'student/tracking/:id',
        name: 'StudentFeedbackDetail',
        component: () => import('@/pages/student/FeedbackDetailPage.vue'),
        meta: { title: '反馈详情', requiredUserType: 'student' }
      },
      /* 管理员端 */
      {
        path: 'admin/feedback',
        name: 'AdminFeedback',
        component: () => import('@/pages/admin/FeedbackListPage.vue'),
        meta: { title: '反馈查看', requiredRoles: ['SYSTEM_ADMIN', 'ROLE_ADMIN', 'CATEGORY_ADMIN'] }
      },
      {
        path: 'admin/feedback/:id',
        name: 'AdminFeedbackDetail',
        component: () => import('@/pages/admin/FeedbackDetailPage.vue'),
        meta: { title: '反馈详情', requiredRoles: ['SYSTEM_ADMIN', 'ROLE_ADMIN', 'CATEGORY_ADMIN'] }
      },
      {
        path: 'admin/statistics',
        name: 'AdminStatistics',
        component: () => import('@/pages/admin/StatisticsPage.vue'),
        meta: { title: '反馈统计', requiredRoles: ['SYSTEM_ADMIN', 'ROLE_ADMIN'] }
      },
      {
        path: 'admin/statistics/teacher/:id',
        name: 'TeacherFeedbackDetail',
        component: () => import('@/pages/admin/TeacherDetailPage.vue'),
        meta: { title: '教师被反馈详情', requiredRoles: ['SYSTEM_ADMIN', 'ROLE_ADMIN'] }
      },
      {
        path: 'admin/category',
        name: 'AdminCategory',
        component: () => import('@/pages/admin/CategoryPage.vue'),
        meta: { title: '反馈管理', requiredRoles: ['SYSTEM_ADMIN', 'ROLE_ADMIN'] }
      },
      /* 系统管理（子Tab导航） */
      {
        path: 'system',
        component: () => import('@/pages/system/SystemLayout.vue'),
        meta: { requiredRoles: ['SYSTEM_ADMIN'] },
        redirect: '/system/user',
        children: [
          {
            path: 'user',
            name: 'SystemUser',
            component: () => import('@/pages/system/UserPage.vue'),
            meta: { title: '用户管理', requiredRoles: ['SYSTEM_ADMIN'] }
          },
          {
            path: 'semester',
            name: 'SystemSemester',
            component: () => import('@/pages/system/SemesterPage.vue'),
            meta: { title: '学期管理', requiredRoles: ['SYSTEM_ADMIN'] }
          },
          {
            path: 'grade',
            name: 'SystemGrade',
            component: () => import('@/pages/system/GradePage.vue'),
            meta: { title: '年级管理', requiredRoles: ['SYSTEM_ADMIN'] }
          },
          {
            path: 'class',
            name: 'SystemClass',
            component: () => import('@/pages/system/ClassPage.vue'),
            meta: { title: '班级管理', requiredRoles: ['SYSTEM_ADMIN'] }
          },
          {
            path: 'relation',
            name: 'SystemRelation',
            component: () => import('@/pages/system/RelationPage.vue'),
            meta: { title: '师生关系管理', requiredRoles: ['SYSTEM_ADMIN'] }
          },
          {
            path: 'role',
            name: 'SystemRole',
            component: () => import('@/pages/system/RolePage.vue'),
            meta: { title: '角色权限设置', requiredRoles: ['SYSTEM_ADMIN'] }
          },
          {
            path: 'category-permission',
            name: 'SystemCategoryPermission',
            component: () => import('@/pages/system/CategoryPermissionPage.vue'),
            meta: { title: '类别权限配置', requiredRoles: ['SYSTEM_ADMIN', 'ROLE_ADMIN'] }
          },
          {
            path: 'quick-reply',
            name: 'SystemQuickReply',
            component: () => import('@/pages/admin/QuickReplyPage.vue'),
            meta: { title: '快捷回复管理', requiredRoles: ['SYSTEM_ADMIN', 'ROLE_ADMIN', 'CATEGORY_ADMIN'] }
          }
        ]
      },
      /* 无权限提示页 */
      {
        path: '/no-permission',
        name: 'NoPermission',
        component: () => import('@/pages/NoPermissionPage.vue')
      }
    ]
  },
  /* 默认重定向 */
  {
    path: '/:pathMatch(.*)*',
    redirect: '/login'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

/* 路由守卫：JWT 认证检查 + 刷新后恢复用户信息 */
router.beforeEach(async (to, _from, next) => {
  const userStore = useUserStore()
  if (to.meta.requiresAuth !== false && !userStore.token) {
    next('/login')
    return
  }
  /* token 存在但 userInfo 丢失（页面刷新），从后端重新获取 */
  if (userStore.token && !userStore.userInfo) {
    try {
      const res = await getUserInfoApi()
      if (res.data.code === 200 && res.data.data) {
        userStore.setUserInfo(res.data.data)
      } else {
        userStore.logout()
        next('/login')
        return
      }
    } catch {
      userStore.logout()
      next('/login')
      return
    }
  }
  /* 角色权限检查 */
  const requiredRoles = to.meta.requiredRoles as string[] | undefined
  const requiredUserType = to.meta.requiredUserType as string | undefined

  if (requiredUserType && userStore.userInfo?.userType !== requiredUserType) {
    if (userStore.isAdmin) {
      next('/admin/feedback')
    } else {
      next('/student/feedback')
    }
    return
  }

  if (requiredRoles && requiredRoles.length > 0 && !userStore.hasAnyRole(requiredRoles)) {
    if (userStore.userInfo?.userType === 'student') {
      next('/student/feedback')
    } else if (userStore.isAdmin) {
      next('/admin/feedback')
    } else {
      next('/no-permission')
    }
    return
  }

  next()
})

export default router
