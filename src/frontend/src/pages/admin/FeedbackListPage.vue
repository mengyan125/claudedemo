<template>
  <div class="feedback-list-page">
    <!-- 筛选区域 -->
    <div class="filter-card">
      <div class="filter-row">
        <span class="filter-label">类别：</span>
        <el-select v-model="filterCategory" placeholder="教学" clearable size="small" style="width:140px" @change="handleFilter">
          <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
        <span class="filter-label">年级</span>
        <el-select v-model="filterGrade" placeholder="一年级" clearable size="small" style="width:120px" @change="handleFilter">
          <el-option label="高一" value="高一" /><el-option label="高二" value="高二" />
        </el-select>
        <span class="filter-label">班级</span>
        <el-select v-model="filterClass" placeholder="全部班级" clearable size="small" style="width:120px" @change="handleFilter">
          <el-option label="高一（1班）" value="1" /><el-option label="高一（3班）" value="3" />
        </el-select>
        <span class="filter-label">反馈对象</span>
        <el-select v-model="filterTarget" placeholder="全部" clearable size="small" style="width:100px" @change="handleFilter">
          <el-option label="英语—李学" value="1" /><el-option label="数学—王老师" value="2" />
        </el-select>
        <el-input v-model="keyword" placeholder="搜索关键词" clearable size="small" style="width:220px" @keyup.enter="handleFilter" @clear="handleFilter">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
      </div>

      <div class="filter-row">
        <span class="filter-label">时间：</span>
        <el-date-picker v-model="dateStart" type="date" placeholder="2025-03-01" size="small" style="width:140px" />
        <span class="date-sep">至</span>
        <el-date-picker v-model="dateEnd" type="date" placeholder="2025-04-07" size="small" style="width:140px" />
      </div>

      <div class="filter-row">
        <span class="filter-label">状态：</span>
        <span
          v-for="s in statusTabs" :key="s.value"
          class="tab-item" :class="{ active: filterStatus === s.value }"
          @click="filterStatus = s.value; handleFilter()"
        >{{ s.label }}</span>
        <span class="tab-spacer" />
        <span class="filter-label">回复情况：</span>
        <span
          v-for="r in replyTabs" :key="r.value"
          class="tab-item" :class="{ active: filterReply === r.value }"
          @click="filterReply = r.value; handleFilter()"
        >{{ r.label }}</span>
      </div>

      <div class="filter-row">
        <span class="link-item">我的收藏</span>
        <span class="link-item">提醒关注</span>
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
            <span class="item-title" @click="goDetail(item.id)">{{ item.title }}</span>
            <span class="item-spacer" />
            <span class="item-fav">♡ 收藏</span>
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
import { Search } from '@element-plus/icons-vue'
import { getAdminFeedbackListApi, getCategoryListApi } from '@/api/admin'
import type { AdminFeedbackItem, CategoryItem } from '@/api/admin'

const router = useRouter()
const loading = ref(false)
const categories = ref<CategoryItem[]>([])
const feedbackList = ref<AdminFeedbackItem[]>([])
const filterCategory = ref<number | undefined>(undefined)
const filterGrade = ref('')
const filterClass = ref('')
const filterTarget = ref('')
const keyword = ref('')
const dateStart = ref('')
const dateEnd = ref('')
const filterStatus = ref('')
const filterReply = ref('')

const statusTabs = [
  { label: '全部（3条）', value: '' },
  { label: '未读(2条)', value: 'unread' },
  { label: '已读（1条）', value: 'read' }
]
const replyTabs = [
  { label: '全部（3条）', value: '' },
  { label: '未回复(2条)', value: 'submitted' },
  { label: '已回复(1条)', value: 'replied' }
]

function formatDate(dateStr: string): string {
  const d = new Date(dateStr)
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日`
}

function goDetail(id: number) {
  router.push(`/admin/feedback/${id}`)
}

async function fetchList() {
  loading.value = true
  try {
    const { data } = await getAdminFeedbackListApi({
      categoryId: filterCategory.value,
      status: filterReply.value,
      keyword: keyword.value,
      pageNum: 1,
      pageSize: 20
    })
    feedbackList.value = data.data.list
  } catch { /* 错误已在拦截器处理 */ }
  finally { loading.value = false }
}

function handleFilter() { fetchList() }

function goFeedback() { /* placeholder */ }

onMounted(async () => {
  try {
    const { data } = await getCategoryListApi()
    categories.value = data.data
  } catch { /* 错误已在拦截器处理 */ }
  fetchList()
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

.tab-spacer { width: 40px; }

.link-item {
  font-size: 13px;
  color: #333;
  cursor: pointer;
}

.link-item:hover { color: #2AABCB; }

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
