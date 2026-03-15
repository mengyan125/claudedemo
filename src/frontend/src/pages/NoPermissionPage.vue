<template>
  <div class="no-permission-page">
    <div class="content-card">
      <img src="@/assets/no-permission.png" alt="" class="illustration" />
      <h2 class="title">暂无可用功能</h2>
      <p class="desc">您的账号尚未分配管理权限，请联系系统管理员开通相关功能。</p>
      <el-button round class="logout-btn" @click="handleLogout">退出登录</el-button>
      <span class="footer-hint">如有疑问，请联系管理员</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

function handleLogout() {
  userStore.logout()
  router.replace('/login')
}
</script>

<style scoped>
.no-permission-page {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  background: #f5f5f5;
}

.content-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: 16px;
}

.illustration {
  width: 260px;
  height: 200px;
  object-fit: contain;
  margin-bottom: 8px;
}

.title {
  font-size: 22px;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

.desc {
  font-size: 14px;
  color: #6b7280;
  line-height: 1.6;
  max-width: 320px;
  margin: 0;
}

.logout-btn {
  margin-top: 8px;
  min-width: 160px;
  border-color: #dcdfe6;
  color: #333;
}

.footer-hint {
  font-size: 12px;
  color: #9ca3af;
  margin-top: 4px;
}
</style>
