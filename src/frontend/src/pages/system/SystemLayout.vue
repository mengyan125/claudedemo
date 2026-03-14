<template>
  <SubTabLayout :tabs="tabs" />
</template>

<script setup lang="ts">
import SubTabLayout from '@/layout/SubTabLayout.vue'
import type { SubTab } from '@/layout/SubTabLayout.vue'

const tabs: SubTab[] = [
  { label: '用户管理', path: '/system/user' },
  { label: '学期管理', path: '/system/semester' },
  { label: '年级管理', path: '/system/grade' },
  { label: '班级管理', path: '/system/class' },
  { label: '师生关系', path: '/system/relation' },
  { label: '角色权限', path: '/system/role' },
  { label: '快捷回复', path: '/system/quick-reply' }
]
</script>
