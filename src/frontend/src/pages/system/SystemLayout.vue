<template>
  <SubTabLayout :tabs="tabs" />
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import SubTabLayout from '@/layout/SubTabLayout.vue'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

/* 系统管理子Tab配置（含角色过滤） */
const allSystemTabs = [
  // { label: '学期管理', path: '/system/semester', requiredRoles: ['SYSTEM_ADMIN'] },
  { label: '年级管理', path: '/system/grade', requiredRoles: ['SYSTEM_ADMIN'] },
  { label: '班级管理', path: '/system/class', requiredRoles: ['SYSTEM_ADMIN'] },
  { label: '师生关系', path: '/system/relation', requiredRoles: ['SYSTEM_ADMIN'] },
  // { label: '角色权限', path: '/system/role', requiredRoles: ['SYSTEM_ADMIN'] },
  { label: '角色管理', path: '/system/user', requiredRoles: ['SYSTEM_ADMIN', 'ROLE_ADMIN'] },
  { label: '类别权限', path: '/system/category-permission', requiredRoles: ['SYSTEM_ADMIN', 'ROLE_ADMIN'] },
  { label: '快捷回复', path: '/system/quick-reply', requiredRoles: ['SYSTEM_ADMIN', 'ROLE_ADMIN', 'CATEGORY_ADMIN'] },
]

const tabs = computed(() =>
  allSystemTabs
    .filter(tab => userStore.hasAnyRole(tab.requiredRoles))
    .map(tab => ({ label: tab.label, path: tab.path }))
)

/* 检查当前路由是否有权限，无权限则重定向到有权限的第一个子页面 */
onMounted(() => {
  const currentPath = route.path
  const currentTab = allSystemTabs.find(tab => currentPath.startsWith(tab.path))
  const hasAccess = currentTab ? userStore.hasAnyRole(currentTab.requiredRoles) : false
  if (!hasAccess && tabs.value.length > 0) {
    router.replace(tabs.value[0].path)
  }
})
</script>
