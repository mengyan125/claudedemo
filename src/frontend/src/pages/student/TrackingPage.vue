<template>
  <div class="tracking-page">
    <!-- 筛选行 -->
    <div class="filter-row">
      <div class="filter-left">
        <span class="filter-label">类别：</span>
        <el-select v-model="filterCategoryId" placeholder="全部类别" clearable @change="handleFilter">
          <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
      </div>
      <el-input
        v-model="keyword"
        placeholder="关键词搜素"
        class="search-input"
        clearable
        @keyup.enter="handleFilter"
        @clear="handleFilter"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
    </div>

    <!-- 反馈列表 -->
    <div v-loading="loading" class="feedback-list">
      <!-- 空状态 -->
      <div v-if="!loading && feedbackList.length === 0" class="empty-state">
        亲爱的同学，你还没有提过反馈意见哦。
      </div>

      <!-- 列表项 -->
      <div
        v-for="item in feedbackList"
        :key="item.id"
        class="feedback-item"
        @click="handleItemClick(item)"
      >
        <div class="item-header">
          <span class="item-title">{{ item.title }}</span>
          <span v-if="item.hasUnread" class="unread-badge">未读</span>
          <span v-if="item.status === 'draft'" class="draft-badge">暂存</span>
          <span class="item-spacer" />
          <span class="item-meta">反馈类别：{{ item.categoryName }}</span>
          <span v-if="item.teacherName" class="item-meta">反馈对象：{{ item.teacherName }}</span>
          <span class="item-meta">时间：{{ formatDate(item.createTime) }}</span>
        </div>
        <div class="item-content" :title="item.content">{{ item.content }}</div>
      </div>

      <!-- 加载更多 -->
      <div v-if="feedbackList.length > 0 && hasMore" class="load-more">
        <el-button :loading="loadingMore" text @click="loadMore">加载更多</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { getFeedbackListApi, getStudentCategoriesApi } from '@/api/student'
import type { FeedbackListItem, StudentCategory } from '@/api/student'

const router = useRouter()
const loading = ref(false)
const loadingMore = ref(false)

const categories = ref<StudentCategory[]>([])
const feedbackList = ref<FeedbackListItem[]>([])
const filterCategoryId = ref<number | undefined>(undefined)
const keyword = ref('')
const pageNum = ref(1)
const pageSize = 10
const total = ref(0)
const hasMore = ref(false)

/* 日期格式化：显示年月日 */
function formatDate(dateStr: string): string {
  const d = new Date(dateStr)
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日`
}

/* 获取列表 */
async function fetchList(append = false) {
  if (append) {
    loadingMore.value = true
  } else {
    loading.value = true
  }
  try {
    const { data } = await getFeedbackListApi({
      categoryId: filterCategoryId.value,
      keyword: keyword.value,
      pageNum: pageNum.value,
      pageSize
    })
    const result = data.data
    if (append) {
      feedbackList.value.push(...result.list)
    } else {
      feedbackList.value = result.list
    }
    total.value = result.total
    hasMore.value = feedbackList.value.length < result.total
  } catch {
    /* 错误已在拦截器处理 */
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

/* 筛选/搜索 */
function handleFilter() {
  pageNum.value = 1
  fetchList()
}

/* 加载更多 */
function loadMore() {
  pageNum.value++
  fetchList(true)
}

/* 点击列表项 */
function handleItemClick(item: FeedbackListItem) {
  if (item.status === 'draft') {
    router.push(`/student/feedback/edit/${item.id}`)
  } else {
    router.push(`/student/tracking/${item.id}`)
  }
}

/* 初始化 */
onMounted(async () => {
  try {
    const { data } = await getStudentCategoriesApi()
    categories.value = data.data
  } catch {
    /* 错误已在拦截器处理 */
  }
  fetchList()
})
</script>

<style scoped>
.tracking-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
  height: 100%;
}

.filter-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-left {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}

.filter-label {
  font-size: 14px;
  color: #333;
  white-space: nowrap;
}

.filter-left :deep(.el-select) {
  width: 180px;
}

.search-input {
  width: 280px;
}

.feedback-list {
  background: #fff;
  border-radius: 8px;
  border: 1px solid #e8e8e8;
  flex: 1;
}

.empty-state {
  text-align: center;
  padding: 80px 0;
  color: #999;
  font-size: 14px;
}

.feedback-item {
  padding: 20px 28px;
  border-bottom: 1px solid #eee;
  cursor: pointer;
  transition: background 0.2s;
}

.feedback-item:last-child {
  border-bottom: none;
}

.feedback-item:hover {
  background: #fafafa;
}

.item-header {
  display: flex;
  align-items: center;
  gap: 8px;
}

.item-title {
  font-size: 16px;
  font-weight: 700;
  color: #333;
}

.unread-badge {
  display: inline-block;
  background: #FF4D4F;
  color: #fff;
  font-size: 10px;
  font-weight: 600;
  padding: 2px 6px;
  border-radius: 9px;
}

.draft-badge {
  display: inline-block;
  background: #E6A23C;
  color: #fff;
  font-size: 10px;
  font-weight: 600;
  padding: 2px 6px;
  border-radius: 9px;
}

.item-spacer {
  flex: 1;
}

.item-meta {
  font-size: 13px;
  color: #666;
  margin-left: 16px;
}

.item-content {
  font-size: 13px;
  color: #999;
  margin-top: 15px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.load-more {
  text-align: center;
  padding: 16px 0;
}
</style>
