<template>
  <div class="main-layout">
    <!-- 蓝色导航栏 -->
    <header class="nav-bar">
      <!-- 左侧 Logo -->
      <div class="nav-left">
        <el-icon :size="24" color="#fff"><Grid /></el-icon>
        <span class="nav-title">意见反馈</span>
      </div>

      <!-- 中间 Tab 导航 -->
      <nav class="nav-tabs">
        <router-link
          v-for="tab in visibleTabs"
          :key="tab.path"
          :to="tab.path"
          :class="['nav-tab', { active: isActiveTab(tab) }]"
        >
          {{ tab.label }}
        </router-link>
      </nav>

      <!-- 右侧信息区 -->
      <div class="nav-right">
        <el-icon :size="20" class="nav-icon"><Bell /></el-icon>
        <el-dropdown trigger="click" @command="handleCommand">
          <span class="user-info">
            {{ userStore.userInfo?.realName || '用户' }}
            <el-icon :size="14"><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <el-icon :size="20" class="nav-icon" @click="showSettings = true">
          <Setting />
        </el-icon>
      </div>
    </header>

    <!-- 内容区域 -->
    <main class="main-content" :class="{ 'no-padding': isNestedLayout }">
      <router-view />
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Grid, Bell, ArrowDown, Setting } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { logoutApi } from '@/api/auth'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const showSettings = ref(false)

/* Tab 导航配置 */
interface TabItem {
  path: string
  label: string
  match: string[] // 用于匹配激活状态的路由前缀
  requiredUserType?: string
  requiredRoles?: string[]
}

const allTabs: TabItem[] = [
  { path: '/student/feedback', label: '我要反馈', match: ['/student/feedback'], requiredUserType: 'student' },
  { path: '/student/tracking', label: '反馈跟踪', match: ['/student/tracking'], requiredUserType: 'student' },
  { path: '/admin/feedback', label: '反馈查看', match: ['/admin/feedback'], requiredRoles: ['SYSTEM_ADMIN', 'ROLE_ADMIN', 'CATEGORY_ADMIN'] },
  { path: '/admin/statistics', label: '反馈统计', match: ['/admin/statistics'], requiredRoles: ['SYSTEM_ADMIN', 'ROLE_ADMIN'] },
  { path: '/admin/category', label: '反馈管理', match: ['/admin/category'], requiredRoles: ['SYSTEM_ADMIN', 'ROLE_ADMIN'] },
  { path: '/system', label: '系统管理', match: ['/system'], requiredRoles: ['SYSTEM_ADMIN', 'ROLE_ADMIN'] },
]

/* 根据角色过滤可见 Tab */
const visibleTabs = computed(() => {
  const info = userStore.userInfo
  if (!info) return []
  return allTabs.filter(tab => {
    if (tab.requiredUserType) {
      return info.userType === tab.requiredUserType
    }
    if (tab.requiredRoles) {
      return userStore.hasAnyRole(tab.requiredRoles)
    }
    return false
  })
})

/* 判断 Tab 是否激活 */
function isActiveTab(tab: TabItem): boolean {
  return tab.match.some(prefix => route.path.startsWith(prefix))
}

/* 是否为嵌套布局路由（系统管理/反馈管理含子Tab） */
const isNestedLayout = computed(() => {
  return route.path.startsWith('/system') || route.path.startsWith('/admin/category') || route.path.startsWith('/admin/quick-reply')
})

/* 下拉菜单命令 */
async function handleCommand(command: string) {
  if (command === 'logout') {
    try {
      await logoutApi()
    } catch {
      /* 忽略登出接口错误 */
    }
    userStore.logout()
    router.push('/login')
  }
}
</script>

<style scoped>
.main-layout {
  display: flex;
  flex-direction: column;
  height: 100vh;
  width: 100%;
}

/* 导航栏 */
.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 56px;
  padding: 0 24px;
  background: #2AABCB;
  flex-shrink: 0;
}

.nav-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.nav-title {
  font-size: 22px;
  font-weight: 700;
  color: #fff;
}

/* Tab 导航 */
.nav-tabs {
  display: flex;
  align-items: center;
  height: 100%;
}

.nav-tab {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 24px;
  height: 100%;
  font-size: 16px;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.8);
  text-decoration: none;
  transition: color 0.2s;
  box-sizing: border-box;
  border-bottom: 3px solid transparent;
}

.nav-tab:hover {
  color: #fff;
}

.nav-tab.active {
  color: #fff;
  font-weight: 600;
  border-bottom-color: #fff;
}

/* 右侧信息区 */
.nav-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.nav-icon {
  color: rgba(255, 255, 255, 0.8);
  cursor: pointer;
}

.nav-icon:hover {
  color: #fff;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #fff;
  cursor: pointer;
}

/* 内容区域 */
.main-content {
  flex: 1;
  overflow: auto;
  background: #f5f5f5;
  padding: 24px 40px;
}

.main-content.no-padding {
  padding: 0;
}
</style>
