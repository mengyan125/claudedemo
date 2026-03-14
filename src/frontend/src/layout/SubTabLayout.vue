<template>
  <div class="sub-tab-layout">
    <div class="sub-tab-bar">
      <div
        v-for="tab in tabs"
        :key="tab.path"
        class="sub-tab-item"
        :class="{ active: isActive(tab.path) }"
        @click="router.push(tab.path)"
      >
        {{ tab.label }}
      </div>
    </div>
    <div class="sub-tab-content">
      <router-view />
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter, useRoute } from 'vue-router'

export interface SubTab {
  label: string
  path: string
}

defineProps<{ tabs: SubTab[] }>()

const router = useRouter()
const route = useRoute()

function isActive(path: string) {
  return route.path === path || route.path.startsWith(path + '/')
}
</script>

<style scoped>
.sub-tab-layout {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.sub-tab-bar {
  display: flex;
  gap: 0;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  padding: 0 24px;
  flex-shrink: 0;
}

.sub-tab-item {
  padding: 12px 20px;
  font-size: 14px;
  color: #666;
  cursor: pointer;
  border-bottom: 2px solid transparent;
  transition: color 0.2s, border-color 0.2s;
  white-space: nowrap;
}

.sub-tab-item:hover {
  color: #2AABCB;
}

.sub-tab-item.active {
  color: #2AABCB;
  font-weight: 500;
  border-bottom-color: #2AABCB;
}

.sub-tab-content {
  flex: 1;
  overflow: auto;
  padding: 20px 24px;
  background: #f5f7fa;
}
</style>
