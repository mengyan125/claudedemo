<template>
  <div v-loading="loading" class="teacher-detail-page">
    <!-- 面包屑 -->
    <div class="breadcrumb">
      <span class="breadcrumb-link" @click="router.push('/admin/statistics')">反馈统计</span>
      <span class="breadcrumb-sep">&gt;</span>
      <span>查看详情</span>
    </div>

    <!-- 列表卡片 -->
    <div class="list-card">
      <!-- 标题行 -->
      <div class="page-header">
        <span class="header-icon">≡</span>
        <span class="header-title">{{ teacher.name }}的被反馈记录（{{ teacher.totalCount }}条）</span>
      </div>

      <!-- 空状态 -->
      <div v-if="!loading && feedbackList.length === 0" class="empty-state">暂无反馈记录</div>

      <!-- 反馈列表 -->
      <div
        v-for="item in feedbackList"
        :key="item.id"
        class="feedback-item"
        :class="{ 'has-badge': item.hasUnread }"
      >
        <div class="item-content">
          <div class="item-title-row">
            <span class="item-title" @click="goDetail(item.id)">{{ item.title }}</span>
            <span class="item-spacer" />
            <span
              class="item-fav"
              :class="{ favorited: item.isFavorited }"
              @click.stop="toggleFavorite(item)"
            >
              {{ item.isFavorited ? '❤ 已收藏' : '♡ 收藏' }}
            </span>
          </div>
          <div class="item-meta-row">
            <span class="meta">类别：{{ item.categoryName }}</span>
            <span class="meta">年级：{{ item.gradeName }}</span>
            <span class="meta">班级：{{ item.className }}</span>
            <span class="meta">反馈对象：{{ item.teacherName }}</span>
            <span class="meta">学生：{{ item.isAnonymous ? '匿名' : item.studentName }}</span>
            <span class="meta">时间：{{ formatDate(item.createTime) }}</span>
          </div>
        </div>
        <div v-if="item.hasUnread" class="unread-badge">未读</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getTeacherFeedbackListApi, toggleFavoriteApi } from '@/api/admin'
import type { TeacherFeedbackItem, TeacherInfo } from '@/api/admin'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const loading = ref(false)

const teacher = reactive<TeacherInfo>({ id: 0, name: '', subject: '', totalCount: 0 })
const feedbackList = ref<TeacherFeedbackItem[]>([])

function formatDate(dateStr: string): string {
  const d = new Date(dateStr)
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日`
}

function goDetail(id: number) {
  router.push(`/admin/feedback/${id}`)
}

async function toggleFavorite(item: TeacherFeedbackItem) {
  try {
    await toggleFavoriteApi(item.id)
    item.isFavorited = !item.isFavorited
    ElMessage.success(item.isFavorited ? '收藏成功' : '已取消收藏')
  } catch {
    // 错误已在拦截器处理
  }
}

onMounted(async () => {
  const teacherId = Number(route.params.id)
  loading.value = true
  try {
    const { data } = await getTeacherFeedbackListApi(teacherId)
    Object.assign(teacher, data.data.teacher)
    feedbackList.value = data.data.list
  } catch { /* 错误已在拦截器处理 */ }
  finally { loading.value = false }
})
</script>

<style scoped>
.teacher-detail-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
  height: 100%;
}

.breadcrumb { font-size: 13px; color: #666; }
.breadcrumb-link { cursor: pointer; }
.breadcrumb-link:hover { color: #2AABCB; }
.breadcrumb-sep { margin: 0 4px; }

.list-card {
  background: #fff;
  border-radius: 8px;
  border: 1px solid #e8e8e8;
  padding: 24px 32px;
  flex: 1;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding-bottom: 16px;
}

.header-icon { font-size: 18px; color: #333; }
.header-title { font-size: 18px; font-weight: 700; color: #333; }

.empty-state { text-align: center; padding: 80px 0; color: #999; font-size: 14px; }

.feedback-item {
  display: flex;
  align-items: flex-start;
  padding: 20px 0;
  border-top: 1px solid #f0f0f0;
  position: relative;
}

.item-content { flex: 1; display: flex; flex-direction: column; gap: 10px; }

.item-title-row { display: flex; align-items: center; }
.item-title {
  font-size: 16px;
  font-weight: 700;
  color: #333;
  cursor: pointer;
}
.item-title:hover { color: #2AABCB; }

.item-spacer { flex: 1; }

.item-fav { font-size: 13px; color: #999; cursor: pointer; white-space: nowrap; }
.item-fav:hover { color: #2AABCB; }
.item-fav.favorited { color: #FF4D4F; }

.item-meta-row { display: flex; align-items: center; gap: 24px; }
.meta { font-size: 13px; color: #999; }

.unread-badge {
  background: #FF4D4F;
  color: #fff;
  font-size: 10px;
  font-weight: 700;
  padding: 2px 8px;
  border-radius: 0 0 0 8px;
  position: absolute;
  top: 0;
  right: 0;
}
</style>
