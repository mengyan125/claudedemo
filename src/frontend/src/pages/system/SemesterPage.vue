<template>
  <div class="semester-page">
    <!-- 工具栏 -->
    <div class="toolbar">
      <span class="page-title">学期列表</span>
      <el-button type="primary" round @click="openCreateDialog">
        <el-icon><Plus /></el-icon>新增学期
      </el-button>
    </div>

    <!-- 学期表格 -->
    <el-table v-loading="loading" :data="semesterList" stripe class="data-table">
      <el-table-column prop="semesterName" label="学期名称" min-width="200" />
      <el-table-column prop="startDate" label="开始日期" width="160" />
      <el-table-column prop="endDate" label="结束日期" width="160" />
      <el-table-column label="当前学期" width="120">
        <template #default="{ row }">
          <el-tag v-if="row.isCurrent" type="success" size="small">当前</el-tag>
          <span v-else class="text-muted">—</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" min-width="180">
        <template #default="{ row }">
          <el-button
            v-if="!row.isCurrent"
            link type="primary"
            @click="handleSetCurrent(row)"
          >设为当前</el-button>
          <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button
            v-if="!row.isCurrent"
            link type="danger"
            @click="handleDelete(row)"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editingItem ? '编辑学期' : '新增学期'"
      width="480px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="90px"
      >
        <el-form-item label="学期名称" prop="semesterName">
          <el-input v-model="formData.semesterName" placeholder="如：2025-2026学年第一学期" />
        </el-form-item>
        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker
            v-model="formData.startDate"
            type="date"
            placeholder="选择开始日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="结束日期" prop="endDate">
          <el-date-picker
            v-model="formData.endDate"
            type="date"
            placeholder="选择结束日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  getSemesterListApi,
  createSemesterApi,
  setCurrentSemesterApi,
  deleteSemesterApi
} from '@/api/base'
import type { SemesterItem } from '@/api/base'

const loading = ref(false)
const semesterList = ref<SemesterItem[]>([])

/* 弹窗状态 */
const dialogVisible = ref(false)
const submitting = ref(false)
const editingItem = ref<SemesterItem | null>(null)
const formRef = ref<FormInstance>()
const formData = ref({
  semesterName: '',
  startDate: '',
  endDate: ''
})

const formRules: FormRules = {
  semesterName: [{ required: true, message: '请输入学期名称', trigger: 'blur' }],
  startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
  endDate: [{ required: true, message: '请选择结束日期', trigger: 'change' }]
}

/* 获取列表 */
async function fetchList() {
  loading.value = true
  try {
    const { data } = await getSemesterListApi()
    semesterList.value = data.data
  } catch {
    /* 错误已在拦截器处理 */
  } finally {
    loading.value = false
  }
}

/* 新增 */
function openCreateDialog() {
  editingItem.value = null
  formData.value = { semesterName: '', startDate: '', endDate: '' }
  dialogVisible.value = true
}

/* 编辑 */
function handleEdit(row: SemesterItem) {
  editingItem.value = row
  formData.value = {
    semesterName: row.semesterName,
    startDate: row.startDate,
    endDate: row.endDate
  }
  dialogVisible.value = true
}

/* 提交 */
async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await createSemesterApi(formData.value)
    ElMessage.success(editingItem.value ? '更新成功' : '创建成功')
    dialogVisible.value = false
    fetchList()
  } catch {
    /* 错误已在拦截器处理 */
  } finally {
    submitting.value = false
  }
}

/* 设为当前学期 */
async function handleSetCurrent(row: SemesterItem) {
  await ElMessageBox.confirm(`确定将「${row.semesterName}」设为当前学期吗？`, '提示', {
    type: 'warning'
  })
  try {
    await setCurrentSemesterApi(row.id)
    ElMessage.success('设置成功')
    fetchList()
  } catch {
    /* 错误已在拦截器处理 */
  }
}

/* 删除 */
async function handleDelete(row: SemesterItem) {
  await ElMessageBox.confirm(`确定删除学期「${row.semesterName}」吗？`, '提示', {
    type: 'warning'
  })
  try {
    await deleteSemesterApi(row.id)
    ElMessage.success('删除成功')
    fetchList()
  } catch {
    /* 错误已在拦截器处理 */
  }
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.semester-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
  height: 100%;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.data-table {
  flex: 1;
  border-radius: 8px;
  overflow: hidden;
}

.text-muted {
  color: #C0C4CC;
}
</style>
