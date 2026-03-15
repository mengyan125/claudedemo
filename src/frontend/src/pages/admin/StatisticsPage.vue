<template>
  <div v-loading="loading" class="statistics-page">
    <!-- 统计卡片 -->
    <div class="stat-cards">
      <div class="stat-card">
        <div class="stat-num">
          <span class="num blue">{{ stats.monthCount }}</span><span class="unit">条</span>
        </div>
        <div class="stat-desc">今日共收到学生反馈{{ stats.monthCount }}条</div>
      </div>
      <div class="stat-card">
        <div class="stat-num">
          <span class="num orange">{{ stats.totalCount }}</span><span class="unit">条</span>
        </div>
        <div class="stat-desc">本月共收到学生反馈{{ stats.totalCount }}条</div>
      </div>
      <div class="stat-card">
        <div class="stat-num">
          <span class="num green">{{ stats.semesterCount }}</span><span class="unit">条</span>
        </div>
        <div class="stat-desc">本月反馈类别最多是教学，共{{ stats.semesterCount }}条</div>
      </div>
    </div>

    <!-- 图表行 -->
    <div class="chart-row">
      <!-- 饼图 -->
      <div class="chart-card">
        <div class="title-with-bar">
          <span class="title-bar" />
          <h3 class="chart-title">反馈类别占比分布：</h3>
        </div>
        <div class="pie-date-row">
          <span class="pie-date-label">时间段：</span>
          <el-date-picker v-model="pieDateRange" type="daterange" range-separator="至" start-placeholder="2025-03-01" end-placeholder="2025-03-14" size="small" style="width:220px" />
        </div>
        <div class="pie-area">
          <div class="pie-circle" :style="pieGradientStyle" />
        </div>
        <div class="pie-legend">
          <div v-for="item in stats.categoryDistribution" :key="item.name" class="legend-item">
            <span class="legend-dot" :style="{ background: getCategoryColor(item.name) }" />
            <span class="legend-name">{{ item.name }}</span>
          </div>
        </div>
      </div>

      <!-- 柱状图 -->
      <div class="chart-card">
        <div class="title-with-bar">
          <span class="title-bar" />
          <h3 class="chart-title">本学期反馈数量统计：</h3>
        </div>
        <div class="bar-area">
          <div class="bar-y-axis">
            <span v-for="n in 5" :key="n">{{ (5 - n + 1) * 10 }}</span>
          </div>
          <div class="bar-chart">
            <div v-for="item in stats.semesterTrend" :key="item.semester" class="bar-col">
              <div class="bar-wrapper">
                <div class="bar-fill" :style="{ height: getBarHeight(item.count) + '%' }" />
              </div>
              <span class="bar-label">{{ item.semester.replace('学期', '') }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- TOP10 -->
    <div class="top10-card">
      <div class="top10-header">
        <span class="title-bar" />
        <h3 class="chart-title">任课教师被反馈次数 TOP10：</h3>
      </div>
      <div class="top10-filter">
        <span class="filter-label">时间</span>
        <el-date-picker v-model="top10Start" type="date" placeholder="2025-09-01" size="small" style="width:140px" />
        <span class="date-sep">至</span>
        <el-date-picker v-model="top10End" type="date" placeholder="2025-04-14" size="small" style="width:140px" />
        <span class="spacer" />
        <el-button size="small" class="btn-export">导出</el-button>
      </div>

      <el-table :data="stats.teacherTop10" class="top10-table" size="small">
        <el-table-column prop="rank" label="序号" width="60" align="center" />
        <el-table-column label="教师" min-width="100" align="center">
          <template #default="{ row }">
            <span class="teacher-link" @click="goTeacher(row.teacherId)">{{ row.teacherName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="subject" label="科目" width="80" align="center" />
        <el-table-column prop="gradeName" label="年级/学期" width="120" align="center" />
        <el-table-column prop="className" label="班级/学级" width="120" align="center" />
        <el-table-column prop="count" label="被反馈次数" width="100" align="center" />
        <el-table-column label="操作" width="80" align="center">
          <template #default="{ row }">
            <span class="view-link" @click="goTeacher(row.teacherId)">查看</span>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getStatisticsApi } from '@/api/admin'
import type { FeedbackStatistics } from '@/api/admin'

const router = useRouter()
const loading = ref(false)
const top10Start = ref('')
const top10End = ref('')
const pieDateRange = ref<[string, string] | null>(null)

const stats = reactive<FeedbackStatistics>({
  totalCount: 0, semesterCount: 0, monthCount: 0,
  categoryDistribution: [], semesterTrend: [], teacherTop10: []
})

const PIE_COLORS = ['#E6A23C', '#9B59B6', '#67C23A', '#409EFF', '#E91E63']
const COLORS: Record<string, string> = {
  '教学': '#E6A23C', '后勤': '#9B59B6', '校园安全': '#67C23A', '学校食堂': '#409EFF', '图书馆': '#E91E63'
}

function getCategoryColor(name: string) { return COLORS[name] || '#909399' }

const pieGradientStyle = computed(() => {
  const dist = stats.categoryDistribution
  if (!dist.length) return { background: '#f0f0f0' }
  const stops: string[] = []
  let acc = 0
  dist.forEach((item, idx) => {
    const color = PIE_COLORS[idx % PIE_COLORS.length]
    stops.push(`${color} ${acc}%`)
    acc += item.percentage
    stops.push(`${color} ${acc}%`)
  })
  return { background: `conic-gradient(${stops.join(', ')})` }
})

function getBarHeight(count: number) {
  const max = Math.max(...stats.semesterTrend.map(s => s.count), 1)
  return (count / max) * 100
}

function goTeacher(id: number) { router.push(`/admin/statistics/teacher/${id}`) }

onMounted(async () => {
  loading.value = true
  try {
    const { data } = await getStatisticsApi()
    Object.assign(stats, data.data)
  } catch { /* 错误已在拦截器处理 */ }
  finally { loading.value = false }
})
</script>

<style scoped>
.statistics-page { display: flex; flex-direction: column; gap: 24px; }

.stat-cards { display: flex; gap: 24px; }
.stat-card {
  flex: 1; background: #fff; border-radius: 8px; border: 1px solid #e8e8e8;
  height: 160px; display: flex; flex-direction: column; align-items: center;
  justify-content: center; gap: 8px;
}
.stat-num { display: flex; align-items: flex-end; gap: 2px; }
.num { font-size: 48px; font-weight: 700; line-height: 1; }
.num.blue { color: #2AABCB; }
.num.orange { color: #E6A23C; }
.num.green { color: #67C23A; }
.unit { font-size: 16px; color: #999; margin-bottom: 6px; }
.stat-desc { font-size: 13px; color: #999; }

.chart-row { display: flex; gap: 24px; }
.chart-card {
  flex: 1; background: #fff; border-radius: 8px; border: 1px solid #e8e8e8;
  height: 420px; padding: 20px 24px; display: flex; flex-direction: column;
}
.title-with-bar { display: flex; align-items: center; gap: 8px; }
.title-bar { width: 4px; height: 18px; background: #2AABCB; border-radius: 2px; flex-shrink: 0; }
.chart-title { font-size: 15px; font-weight: 700; color: #333; margin: 0; }

.pie-date-row { display: flex; align-items: center; gap: 8px; margin-top: 15px; width: 60%; }
.pie-date-label { font-size: 13px; color: #666; }

.pie-area {
  flex: 1; display: flex; align-items: center; justify-content: center;
}
.pie-circle {
  width: 220px; height: 220px; border-radius: 50%;
  background: #f0f0f0; flex-shrink: 0;
}
.pie-legend { display: flex; align-items: center; justify-content: center; gap: 16px; }
.legend-item { display: flex; align-items: center; gap: 4px; font-size: 12px; }
.legend-dot { width: 12px; height: 12px; border-radius: 2px; flex-shrink: 0; }
.legend-name { color: #666; }

.bar-area { flex: 1; display: flex; gap: 8px; padding-top: 16px; }
.bar-y-axis {
  display: flex; flex-direction: column; justify-content: space-between;
  font-size: 11px; color: #999; padding-bottom: 24px;
}
.bar-chart { flex: 1; display: flex; align-items: flex-end; justify-content: space-around; }
.bar-col { display: flex; flex-direction: column; align-items: center; gap: 8px; flex: 1; }
.bar-wrapper {
  width: 40px; height: 240px; background: #f0f0f0; border-radius: 4px 4px 0 0;
  display: flex; align-items: flex-end; overflow: hidden;
}
.bar-fill { width: 100%; background: #E6A23C; border-radius: 4px 4px 0 0; transition: height 0.5s; }
.bar-label { font-size: 11px; color: #666; text-align: center; white-space: nowrap; }

.top10-card {
  background: #fff; border-radius: 8px; border: 1px solid #e8e8e8; padding: 20px 24px;
  display: flex; flex-direction: column; gap: 16px;
}
.top10-header { display: flex; align-items: center; gap: 8px; }
.top10-filter { display: flex; align-items: center; gap: 8px; }
.filter-label { font-size: 13px; color: #333; }
.date-sep { font-size: 13px; color: #666; }
.spacer { flex: 1; }
.btn-export { background: #2AABCB; border-color: #2AABCB; color: #fff; }

.teacher-link { color: #2AABCB; cursor: pointer; font-weight: 600; }
.teacher-link:hover { text-decoration: underline; }
.view-link { color: #2AABCB; cursor: pointer; font-size: 13px; }
.view-link:hover { text-decoration: underline; }
</style>
