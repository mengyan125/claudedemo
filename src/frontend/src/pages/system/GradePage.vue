<template>
  <div class="grade-page">
    <!-- 工具栏 -->
    <div class="toolbar">
      <span class="page-title">年级列表</span>
      <el-button type="primary" round @click="openCreateDialog">
        <el-icon><Plus /></el-icon>新增年级
      </el-button>
    </div>

    <!-- 年级表格 -->
    <el-table v-loading="loading" :data="gradeList" stripe class="data-table">
      <el-table-column prop="gradeName" label="年级名称" min-width="200" />
      <el-table-column prop="sortOrder" label="排序" width="120" />
      <el-table-column label="操作" min-width="160">
        <template #default="{ row }">
          <el-button text class="text-btn-primary" @click="handleEdit(row)">编辑</el-button>
          <el-button text class="text-btn-danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editingItem ? '编辑年级' : '新增年级'"
      width="420px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="80px"
      >
        <el-form-item label="年级名称" prop="gradeName">
          <el-input v-model="formData.gradeName" placeholder="如：一年级" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="formData.sortOrder" :min="1" :max="99" />
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
  getGradeListApi,
  createGradeApi,
  updateGradeApi,
  deleteGradeApi
} from '@/api/base'
import type { GradeItem } from '@/api/base'

const loading = ref(false)
const gradeList = ref<GradeItem[]>([])

/* 弹窗状态 */
const dialogVisible = ref(false)
const submitting = ref(false)
const editingItem = ref<GradeItem | null>(null)
const formRef = ref<FormInstance>()
const formData = ref({
  gradeName: '',
  sortOrder: 1
})

const formRules: FormRules = {
  gradeName: [{ required: true, message: '请输入年级名称', trigger: 'blur' }]
}

/* 获取列表 */
async function fetchList() {
  loading.value = true
  try {
    const { data } = await getGradeListApi()
    gradeList.value = data.data
  } catch {
    /* 错误已在拦截器处理 */
  } finally {
    loading.value = false
  }
}

/* 新增 */
function openCreateDialog() {
  editingItem.value = null
  formData.value = { gradeName: '', sortOrder: 1 }
  dialogVisible.value = true
}

/* 编辑 */
function handleEdit(row: GradeItem) {
  editingItem.value = row
  formData.value = {
    gradeName: row.gradeName,
    sortOrder: row.sortOrder
  }
  dialogVisible.value = true
}

/* 提交 */
async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (editingItem.value) {
      await updateGradeApi(editingItem.value.id, formData.value)
      ElMessage.success('更新成功')
    } else {
      await createGradeApi(formData.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchList()
  } catch {
    /* 错误已在拦截器处理 */
  } finally {
    submitting.value = false
  }
}

/* 删除 */
async function handleDelete(row: GradeItem) {
  await ElMessageBox.confirm(`确定删除年级「${row.gradeName}」吗？`, '提示', {
    type: 'warning'
  })
  try {
    await deleteGradeApi(row.id)
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
.grade-page {
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
.text-btn-primary { color: #2AABCB !important; background: transparent !important; }
.text-btn-primary:hover { color: #24a0bf !important; }
.text-btn-danger { color: #F56C6C !important; background: transparent !important; }
.text-btn-danger:hover { color: #f23c3c !important; }
.text-btn-success { color: #67C23A !important; background: transparent !important; }
.text-btn-success:hover { color: #529b2e !important; }
</style>
