<template>
  <div class="feedback-list-page">
    <!-- 筛选区域 -->
    <div class="filter-card">
      <div class="filter-row">
        <span class="filter-label">类别：</span>
        <el-select v-model="filterCategory" placeholder="全部类别" clearable size="small" style="width:140px" @change="handleFilter">
          <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
        <span class="filter-label">年级</span>
        <el-select v-model="filterGrade" placeholder="全部年级" clearable size="small" style="width:120px" @change="handleGradeChange">
          <el-option v-for="g in grades" :key="g.id" :label="g.gradeName" :value="g.id" />
        </el-select>
        <span class="filter-label">班级</span>
        <el-select v-model="filterClass" placeholder="全部班级" clearable size="small" style="width:140px" @change="handleFilter">
          <el-option v-for="c in classes" :key="c.id" :label="c.className" :value="c.id" />
        </el-select>
        <span class="filter-label">反馈对象</span>
        <el-select
          v-model="filterTarget"
          placeholder="全部"
          clearable
          filterable
          remote
          :remote-method="searchTeachers"
          size="small"
          style="width:160px"
          @change="handleFilter"
        >
          <el-option v-for="t in teacherOptions" :key="t.id" :label="t.realName" :value="t.id" />
        </el-select>
        <el-input v-model="keyword" placeholder="搜索关键词" clearable size="small" style="width:220px" @keyup.enter="handleFilter" @clear="handleFilter">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
      </div>

      <div class="filter-row">
        <span class="filter-label">时间：</span>
        <el-date-picker v-model="dateStart" type="date" placeholder="开始日期" size="small" style="width:140px" value-format="YYYY-MM-DD" @change="handleFilter" />
        <span class="date-sep">至</span>
        <el-date-picker v-model="dateEnd" type="date" placeholder="结束日期" size="small" style="width:140px" value-format="YYYY-MM-DD" @change="handleFilter" />
      </div>

      <div class="filter-row">
        <span class="filter-label">回复情况：</span>
        <span
          v-for="r in replyTabs" :key="r.value"
          class="tab-item" :class="{ active: filterReply === r.value }"
          @click="filterReply = r.value; handleFilter()"
        >{{ r.label }}（{{ r.count }}条）</span>
      </div>

      <div class="filter-row">
        <span class="link-item" :class="{ 'link-active': filterFavorited }" @click="toggleFavoriteFilter">我的收藏</span>
        <span class="link-item" @click="router.push('/admin/reminder')">提醒关注</span>
      </div>
    </div>

    <!-- 反馈列表 -->
    <div v-loading="loading" class="list-card">
      <div v-if="!loading && feedbackList.length === 0" class="empty-state">暂无反馈数据</div>

      <div
        v-for="item in feedbackList" :key="item.id"
        class="feedback-item"
      >
        <div class="item-content">
          <div class="item-title-row">
            <el-icon v-if="item.hasUnreadReminder" class="reminder-bell"><Bell /></el-icon>
            <span class="item-title" @click="goDetail(item.id)">{{ item.title }}</span>
            <span class="item-spacer" />
            <span
              class="item-fav"
              :class="{ 'item-fav-active': item.isFavorited }"
              @click.stop="handleToggleFavorite(item)"
            >{{ item.isFavorited ? '❤ 已收藏' : '♡ 收藏' }}</span>
          </div>
          <div class="item-meta-row">
            <span class="meta">类别：{{ item.categoryName }}</span>
            <span class="meta">年级：{{ item.gradeName }}</span>
            <span class="meta">班级：{{ item.className }}</span>
            <span v-if="item.teacherName" class="meta">反馈对象：{{ item.teacherName }}</span>
            <span class="meta">学生：{{ item.isAnonymous ? '匿名' : item.studentName }}</span>
            <span class="meta">时间：{{ formatDate(item.createTime) }}</span>
          </div>
        </div>
        <div v-if="item.hasUnread" class="unread-badge">未读</div>
        <div v-else-if="item.status === 'replied'" class="replied-badge">已回复</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Bell } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import {
  getAdminFeedbackListApi,
  getCategoryListApi,
  toggleFavoriteApi,
  getStatusCountApi,
  searchUsersApi
} from '@/api/admin'
import type { AdminFeedbackItem, CategoryItem, FeedbackStatusCount, UserSearchItem } from '@/api/admin'
import request from '@/utils/request'
import type { ApiResponse } from '@/utils/request'

/* 年级/班级接口类型 */
interface GradeItem { id: number; gradeName: string }
interface ClassItem { id: number; className: string; gradeId: number }

const router = useRouter()
const loading = ref(false)
const categories = ref<CategoryItem[]>([])
const grades = ref<GradeItem[]>([])
const classes = ref<ClassItem[]>([])
const teacherOptions = ref<UserSearchItem[]>([])
const feedbackList = ref<AdminFeedbackItem[]>([])
const filterCategory = ref<number | undefined>(undefined)
const filterGrade = ref<number | undefined>(undefined)
const filterClass = ref<number | undefined>(undefined)
const filterTarget = ref<number | undefined>(undefined)
const keyword = ref('')
const dateStart = ref('')
const dateEnd = ref('')
const filterReply = ref('')
const filterFavorited = ref(false)

/* 状态统计 */
const statusCount = ref<FeedbackStatusCount>({ totalCount: 0, repliedCount: 0, unrepliedCount: 0 })

const replyTabs = ref([
  { label: '全部', value: '', count: 0 },
  { label: '未回复', value: 'unreplied', count: 0 },
  { label: '已回复', value: 'replied', count: 0 }
])

function formatDate(dateStr: string): string {
  const d = new Date(dateStr)
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日`
}

function goDetail(id: number) {
  router.push(`/admin/feedback/${id}`)
}

function buildFilterParams() {
  return {
    categoryId: filterCategory.value,
    keyword: keyword.value || undefined,
    gradeId: filterGrade.value,
    classId: filterClass.value,
    teacherId: filterTarget.value,
    dateStart: dateStart.value || undefined,
    dateEnd: dateEnd.value || undefined,
    replyStatus: filterReply.value || undefined,
    isFavorited: filterFavorited.value || undefined,
    pageNum: 1,
    pageSize: 20
  }
}

async function fetchList() {
  loading.value = true
  try {
    const { data } = await getAdminFeedbackListApi(buildFilterParams())
    feedbackList.value = data.data.list
  } catch { /* 错误已在拦截器处理 */ }
  finally { loading.value = false }
}

async function fetchStatusCount() {
  try {
    const { data } = await getStatusCountApi({
      categoryId: filterCategory.value,
      keyword: keyword.value || undefined,
      gradeId: filterGrade.value,
      classId: filterClass.value,
      teacherId: filterTarget.value,
      dateStart: dateStart.value || undefined,
      dateEnd: dateEnd.value || undefined
    })
    statusCount.value = data.data
    replyTabs.value = [
      { label: '全部', value: '', count: data.data.totalCount },
      { label: '未回复', value: 'unreplied', count: data.data.unrepliedCount },
      { label: '已回复', value: 'replied', count: data.data.repliedCount }
    ]
  } catch { /* 错误已在拦截器处理 */ }
}

function handleFilter() {
  fetchList()
  fetchStatusCount()
}

async function handleGradeChange() {
  filterClass.value = undefined
  if (filterGrade.value) {
    try {
      const { data } = await request.get<ApiResponse<ClassItem[]>>('/base/class/list', { params: { gradeId: filterGrade.value } })
      classes.value = data.data
    } catch { /* 错误已在拦截器处理 */ }
  } else {
    classes.value = []
  }
  handleFilter()
}

async function searchTeachers(keyword: string) {
  if (!keyword) {
    teacherOptions.value = []
    return
  }
  try {
    const { data } = await searchUsersApi(keyword, 'teacher')
    teacherOptions.value = data.data
  } catch { /* 错误已在拦截器处理 */ }
}

function toggleFavoriteFilter() {
  filterFavorited.value = !filterFavorited.value
  handleFilter()
}

async function handleToggleFavorite(item: AdminFeedbackItem) {
  try {
    await toggleFavoriteApi(item.id)
    item.isFavorited = !item.isFavorited
    ElMessage.success(item.isFavorited ? '已收藏' : '已取消收藏')
  } catch { /* 错误已在拦截器处理 */ }
}

onMounted(async () => {
  /* 并行加载类别和年级列表 */
  try {
    const [catRes, gradeRes] = await Promise.all([
      getCategoryListApi(),
      request.get<ApiResponse<GradeItem[]>>('/base/grade/list')
    ])
    categories.value = catRes.data.data
    grades.value = gradeRes.data.data
  } catch { /* 错误已在拦截器处理 */ }
  fetchList()
  fetchStatusCount()
})
</script>

<style scoped>
.feedback-list-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
  height: 100%;
}

.filter-card {
  background: #fff;
  border-radius: 8px;
  border: 1px solid #e8e8e8;
  padding: 20px 24px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.filter-row {
  display: flex;
  align-items: center;
  gap: 16px;
}

.filter-label {
  font-size: 13px;
  color: #333;
  white-space: nowrap;
}

.date-sep {
  font-size: 13px;
  color: #666;
}

.tab-item {
  font-size: 13px;
  color: #666;
  cursor: pointer;
  white-space: nowrap;
}

.tab-item.active {
  color: #2AABCB;
  font-weight: 600;
}

.tab-item:hover { color: #2AABCB; }

.link-item {
  font-size: 13px;
  color: #333;
  cursor: pointer;
}

.link-item:hover { color: #2AABCB; }
.link-active { color: #2AABCB; font-weight: 600; }

.list-card {
  background: #fff;
  border-radius: 8px;
  border: 1px solid #e8e8e8;
  flex: 1;
  overflow-y: auto;
}

.empty-state {
  text-align: center;
  padding: 80px 0;
  color: #999;
  font-size: 14px;
}

.feedback-item {
  display: flex;
  align-items: flex-start;
  padding: 20px 28px;
  border-bottom: 1px solid #eee;
  cursor: pointer;
  position: relative;
  transition: background 0.2s;
}

.feedback-item:last-child { border-bottom: none; }
.feedback-item:hover { background: #fafafa; }

.item-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.item-title-row { display: flex; align-items: center; }

.reminder-bell {
  color: #E6A23C;
  margin-right: 6px;
  font-size: 16px;
  animation: bell-shake 1s ease-in-out;
}

@keyframes bell-shake {
  0%, 100% { transform: rotate(0); }
  15% { transform: rotate(12deg); }
  30% { transform: rotate(-10deg); }
  45% { transform: rotate(6deg); }
  60% { transform: rotate(-4deg); }
  75% { transform: rotate(2deg); }
}

.item-title {
  font-size: 16px;
  font-weight: 700;
  color: #333;
  cursor: pointer;
}
.item-title:hover { color: #2AABCB; }

.item-spacer { flex: 1; }

.item-fav {
  font-size: 13px;
  color: #999;
  cursor: pointer;
  white-space: nowrap;
  margin-right: 8px;
}
.item-fav:hover { color: #2AABCB; }
.item-fav-active { color: #FF4D4F; }

.item-meta-row { display: flex; align-items: center; gap: 24px; flex-wrap: wrap; }
.meta { font-size: 13px; color: #999; white-space: nowrap; }

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

.replied-badge {
  background: #2AABCB;
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
