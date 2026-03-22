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
          <span class="num green">{{ topCategory.count }}</span><span class="unit">条</span>
        </div>
        <div class="stat-desc">本月反馈类别最多是{{ topCategory.name }}，共{{ topCategory.count }}条</div>
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
          <el-date-picker v-model="pieDateRange" type="daterange" range-separator="至" start-placeholder="2025-03-01" end-placeholder="2025-03-14" size="small" style="width:220px" value-format="YYYY-MM-DD" clearable />
        </div>
        <div class="pie-area">
          <div class="pie-chart-wrapper">
            <div class="pie-circle" :style="pieGradientStyle" @mousemove="onPieMouseMove" @mouseleave="pieTooltip.visible = false" />
            <div v-if="pieTooltip.visible" class="pie-tooltip" :style="{ left: pieTooltip.x + 'px', top: pieTooltip.y + 'px' }">
              <span class="pie-tooltip-dot" :style="{ background: pieTooltip.color }" />
              <span>{{ pieTooltip.name }}：{{ pieTooltip.count }}条（{{ pieTooltip.percentage }}%）</span>
            </div>
          </div>
        </div>
        <div class="pie-legend">
          <div v-for="(item, idx) in stats.categoryDistribution" :key="item.name" class="legend-item">
            <span class="legend-dot" :style="{ background: PIE_COLORS[idx % PIE_COLORS.length] }" />
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
              <div class="bar-wrapper" @mouseenter="item._hover = true" @mouseleave="item._hover = false">
                <div class="bar-tip" v-if="item._hover">{{ item.count }}条</div>
                <div class="bar-fill" :style="{ height: getBarHeight(item.count) + '%' }" />
              </div>
              <span class="bar-label">{{ item.semester }}</span>
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
        <el-date-picker
          v-model="top10DateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          size="small"
          style="width:260px"
          value-format="YYYY-MM-DD"
          @change="fetchTop10"
        />
        <el-button size="small" class="btn-export" @click="exportTop10Excel">导出</el-button>
      </div>

      <el-table :data="top10List" class="top10-table" size="small">
        <el-table-column prop="rank" label="序号" min-width="1" align="center" />
        <el-table-column label="教师" min-width="1" align="center">
          <template #default="{ row }">
            <span class="teacher-link" @click="goTeacher(row.teacherId)">{{ row.teacherName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="subject" label="科目" min-width="1" align="center" />
        <el-table-column prop="categoryName" label="反馈类别" min-width="1" align="center" />
        <el-table-column prop="gradeName" label="年级/学期" min-width="1" align="center" />
        <el-table-column prop="className" label="班级/学级" min-width="1" align="center" />
        <el-table-column prop="count" label="被反馈次数" min-width="1" align="center" />
        <el-table-column label="操作" min-width="1" align="center">
          <template #default="{ row }">
            <span class="view-link" @click="goTeacher(row.teacherId)">查看</span>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { getStatisticsApi, getTeacherTop10Api } from '@/api/admin'
import type { FeedbackStatistics } from '@/api/admin'
import { ElMessage } from 'element-plus'
import * as XLSX from 'xlsx'

const router = useRouter()
const loading = ref(false)
const top10DateRange = ref<[string, string] | null>(null)
const top10List = ref<FeedbackStatistics['teacherTop10']>([])
const pieDateRange = ref<[string, string] | null>(null)

const stats = reactive<FeedbackStatistics>({
  totalCount: 0, semesterCount: 0, monthCount: 0,
  categoryDistribution: [], semesterTrend: [], teacherTop10: []
})

const PIE_COLORS = ['#E6A23C', '#9B59B6', '#67C23A', '#409EFF', '#E91E63']

/* 饼图 tooltip */
const pieTooltip = reactive({ visible: false, x: 0, y: 0, name: '', count: 0, percentage: 0, color: '' })

function onPieMouseMove(e: MouseEvent) {
  const rect = (e.currentTarget as HTMLElement).getBoundingClientRect()
  const cx = rect.width / 2
  const cy = rect.height / 2
  const dx = e.clientX - rect.left - cx
  const dy = e.clientY - rect.top - cy
  // 计算角度（从12点方向顺时针，与 conic-gradient 一致）
  let angle = Math.atan2(dx, -dy) * (180 / Math.PI)
  if (angle < 0) angle += 360
  const pct = angle / 3.6

  const dist = stats.categoryDistribution
  let acc = 0
  for (let i = 0; i < dist.length; i++) {
    acc += dist[i].percentage
    if (pct <= acc) {
      pieTooltip.name = dist[i].name
      pieTooltip.count = dist[i].count
      pieTooltip.percentage = Math.round(dist[i].percentage * 10) / 10
      pieTooltip.color = PIE_COLORS[i % PIE_COLORS.length]
      pieTooltip.visible = true
      pieTooltip.x = e.clientX - rect.left + 12
      pieTooltip.y = e.clientY - rect.top - 30
      return
    }
  }
  pieTooltip.visible = false
}

/* 本月反馈最多的类别 */
const topCategory = computed(() => {
  const dist = stats.categoryDistribution
  if (!dist.length) return { name: '暂无', count: 0 }
  return dist.reduce((max, item) => item.count > max.count ? item : max, dist[0])
})

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

function sanitizeExcelCell(value: unknown): string | number {
  if (typeof value === 'number') {
    return value
  }
  const text = String(value ?? '')
  return /^[=+\-@]/.test(text) ? `'${text}` : text
}

function exportTop10Excel() {
  if (!top10List.value.length) {
    ElMessage.warning('暂无可导出数据')
    return
  }

  const headers = ['序号', '教师', '科目', '反馈类别', '年级/学期', '班级/学级', '被反馈次数']
  const rows = top10List.value.map(item => [
    sanitizeExcelCell(item.rank),
    sanitizeExcelCell(item.teacherName),
    sanitizeExcelCell(item.subject),
    sanitizeExcelCell(item.categoryName),
    sanitizeExcelCell(item.gradeName),
    sanitizeExcelCell(item.className),
    sanitizeExcelCell(item.count)
  ])

  const sheet = XLSX.utils.aoa_to_sheet([headers, ...rows])
  const book = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(book, sheet, 'TOP10')

  const dateTag = new Date().toISOString().slice(0, 10)
  XLSX.writeFile(book, `教师被反馈TOP10_${dateTag}.xlsx`)
  ElMessage.success('导出成功')
}

async function fetchTop10() {
  const params: { startDate?: string; endDate?: string } = {}
  if (top10DateRange.value) {
    params.startDate = top10DateRange.value[0]
    params.endDate = top10DateRange.value[1]
  }
  try {
    const { data } = await getTeacherTop10Api(params)
    top10List.value = data.data
  } catch { /* 错误已在拦截器处理 */ }
}

async function fetchStatistics() {
  const params: { startDate?: string; endDate?: string } = {}
  if (pieDateRange.value) {
    params.startDate = pieDateRange.value[0]
    params.endDate = pieDateRange.value[1]
  }
  loading.value = true
  try {
    const { data } = await getStatisticsApi(params)
    const result = data.data
    result.semesterTrend = result.semesterTrend.map((item: any) => ({ ...item, _hover: false }))
    Object.assign(stats, result)
    top10List.value = result.teacherTop10
  } catch { /* 错误已在拦截器处理 */ }
  finally { loading.value = false }
}

watch(pieDateRange, () => fetchStatistics())

onMounted(() => fetchStatistics())
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
.pie-chart-wrapper {
  position: relative;
}
.pie-circle {
  width: 220px; height: 220px; border-radius: 50%;
  background: #f0f0f0; flex-shrink: 0; cursor: pointer;
}
.pie-tooltip {
  position: absolute; pointer-events: none; white-space: nowrap;
  background: rgba(0, 0, 0, 0.75); color: #fff; font-size: 13px;
  padding: 6px 12px; border-radius: 4px; display: flex; align-items: center; gap: 6px;
  z-index: 10;
}
.pie-tooltip-dot {
  width: 10px; height: 10px; border-radius: 2px; flex-shrink: 0;
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
  display: flex; align-items: flex-end; overflow: visible; position: relative; cursor: pointer;
}
.bar-tip {
  position: absolute; top: -28px; left: 50%; transform: translateX(-50%);
  background: rgba(0, 0, 0, 0.75); color: #fff; font-size: 12px;
  padding: 4px 8px; border-radius: 4px; white-space: nowrap; z-index: 10;
}
.bar-fill { width: 100%; background: #2AABCB; border-radius: 4px 4px 0 0; transition: height 0.5s; }
.bar-label { font-size: 11px; color: #666; text-align: center; white-space: nowrap; }

.top10-card {
  background: #fff; border-radius: 8px; border: 1px solid #e8e8e8; padding: 20px 24px;
  display: flex; flex-direction: column; gap: 16px;
}
.top10-header { display: flex; align-items: center; gap: 8px; }
.top10-filter {
  position: relative;
  min-height: 32px;
  padding-right: 72px;
}

.filter-label {
  display: inline-block;
  margin-right: 8px;
  font-size: 13px;
  color: #333;
}

.btn-export {
  position: absolute;
  right: 0;
  top: 0;
  background: #2AABCB;
  border-color: #2AABCB;
  color: #fff;
}

.teacher-link { color: #2AABCB; cursor: pointer; font-weight: 600; }
.teacher-link:hover { text-decoration: underline; }
.view-link { color: #2AABCB; cursor: pointer; font-size: 13px; }
.view-link:hover { text-decoration: underline; }
</style>
