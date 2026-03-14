import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'

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
        meta: { title: '我要反馈' }
      },
      {
        path: 'student/feedback/edit/:id',
        name: 'StudentFeedbackEdit',
        component: () => import('@/pages/student/FeedbackEditPage.vue'),
        meta: { title: '编辑反馈' }
      },
      {
        path: 'student/tracking',
        name: 'StudentTracking',
        component: () => import('@/pages/student/TrackingPage.vue'),
        meta: { title: '反馈跟踪' }
      },
      {
        path: 'student/tracking/:id',
        name: 'StudentFeedbackDetail',
        component: () => import('@/pages/student/FeedbackDetailPage.vue'),
        meta: { title: '反馈详情' }
      },
      /* 管理员端 */
      {
        path: 'admin/feedback',
        name: 'AdminFeedback',
        component: () => import('@/pages/admin/FeedbackListPage.vue'),
        meta: { title: '反馈查看' }
      },
      {
        path: 'admin/feedback/:id',
        name: 'AdminFeedbackDetail',
        component: () => import('@/pages/admin/FeedbackDetailPage.vue'),
        meta: { title: '反馈详情' }
      },
      {
        path: 'admin/statistics',
        name: 'AdminStatistics',
        component: () => import('@/pages/admin/StatisticsPage.vue'),
        meta: { title: '反馈统计' }
      },
      {
        path: 'admin/statistics/teacher/:id',
        name: 'TeacherFeedbackDetail',
        component: () => import('@/pages/admin/TeacherDetailPage.vue'),
        meta: { title: '教师被反馈详情' }
      },
      {
        path: 'admin/category',
        name: 'AdminCategory',
        component: () => import('@/pages/admin/CategoryPage.vue'),
        meta: { title: '反馈管理' }
      },
      {
        path: 'admin/quick-reply',
        name: 'AdminQuickReply',
        component: () => import('@/pages/admin/QuickReplyPage.vue'),
        meta: { title: '快捷回复管理' }
      },
      /* 系统管理（子Tab导航） */
      {
        path: 'system',
        component: () => import('@/pages/system/SystemLayout.vue'),
        redirect: '/system/user',
        children: [
          {
            path: 'user',
            name: 'SystemUser',
            component: () => import('@/pages/system/UserPage.vue'),
            meta: { title: '用户管理' }
          },
          {
            path: 'semester',
            name: 'SystemSemester',
            component: () => import('@/pages/system/SemesterPage.vue'),
            meta: { title: '学期管理' }
          },
          {
            path: 'grade',
            name: 'SystemGrade',
            component: () => import('@/pages/system/GradePage.vue'),
            meta: { title: '年级管理' }
          },
          {
            path: 'class',
            name: 'SystemClass',
            component: () => import('@/pages/system/ClassPage.vue'),
            meta: { title: '班级管理' }
          },
          {
            path: 'relation',
            name: 'SystemRelation',
            component: () => import('@/pages/system/RelationPage.vue'),
            meta: { title: '师生关系管理' }
          },
          {
            path: 'role',
            name: 'SystemRole',
            component: () => import('@/pages/system/RolePage.vue'),
            meta: { title: '角色权限设置' }
          },
          {
            path: 'category-permission',
            name: 'SystemCategoryPermission',
            component: () => import('@/pages/system/CategoryPermissionPage.vue'),
            meta: { title: '类别权限配置' }
          }
        ]
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

/* 路由守卫：JWT 认证检查 */
router.beforeEach((to, _from, next) => {
  const userStore = useUserStore()
  if (to.meta.requiresAuth !== false && !userStore.token) {
    next('/login')
  } else {
    next()
  }
})

export default router
