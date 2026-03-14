<template>
  <div class="class-page">
    <!-- 工具栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <el-select
          v-model="selectedGradeId"
          placeholder="全部年级"
          clearable
          class="grade-filter"
          @change="fetchList"
        >
          <el-option
            v-for="g in gradeOptions"
            :key="g.id"
            :label="g.gradeName"
            :value="g.id"
          />
        </el-select>
      </div>
      <el-button type="primary" round @click="openCreateDialog">
        <el-icon><Plus /></el-icon>新增班级
      </el-button>
    </div>

    <!-- 班级表格 -->
    <el-table v-loading="loading" :data="classList" stripe class="data-table">
      <el-table-column prop="className" label="班级名称" min-width="200" />
      <el-table-column prop="gradeName" label="所属年级" width="160" />
      <el-table-column prop="sortOrder" label="排序" width="100" />
      <el-table-column label="操作" min-width="160">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editingItem ? '编辑班级' : '新增班级'"
      width="420px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="80px"
      >
        <el-form-item label="所属年级" prop="gradeId">
          <el-select v-model="formData.gradeId" placeholder="请选择年级" style="width: 100%">
            <el-option
              v-for="g in gradeOptions"
              :key="g.id"
              :label="g.gradeName"
              :value="g.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="班级名称" prop="className">
          <el-input v-model="formData.className" placeholder="如：一班" />
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
  getClassListApi,
  createClassApi,
  updateClassApi,
  deleteClassApi,
  getGradeListApi
} from '@/api/base'
import type { ClassItem, GradeItem } from '@/api/base'

const loading = ref(false)
const classList = ref<ClassItem[]>([])
const gradeOptions = ref<GradeItem[]>([])
const selectedGradeId = ref<number | undefined>(undefined)

/* 弹窗状态 */
const dialogVisible = ref(false)
const submitting = ref(false)
const editingItem = ref<ClassItem | null>(null)
const formRef = ref<FormInstance>()
const formData = ref({
  className: '',
  gradeId: undefined as number | undefined,
  sortOrder: 1
})

const formRules: FormRules = {
  className: [{ required: true, message: '请输入班级名称', trigger: 'blur' }],
  gradeId: [{ required: true, message: '请选择所属年级', trigger: 'change' }]
}

/* 获取年级选项 */
async function fetchGrades() {
  try {
    const { data } = await getGradeListApi()
    gradeOptions.value = data.data
  } catch {
    /* 错误已在拦截器处理 */
  }
}

/* 获取班级列表 */
async function fetchList() {
  loading.value = true
  try {
    const { data } = await getClassListApi(selectedGradeId.value)
    classList.value = data.data
  } catch {
    /* 错误已在拦截器处理 */
  } finally {
    loading.value = false
  }
}

/* 新增 */
function openCreateDialog() {
  editingItem.value = null
  formData.value = { className: '', gradeId: undefined, sortOrder: 1 }
  dialogVisible.value = true
}

/* 编辑 */
function handleEdit(row: ClassItem) {
  editingItem.value = row
  formData.value = {
    className: row.className,
    gradeId: row.gradeId,
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
      await updateClassApi(editingItem.value.id, formData.value as Partial<ClassItem>)
      ElMessage.success('更新成功')
    } else {
      await createClassApi(formData.value as Omit<ClassItem, 'id' | 'gradeName'>)
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
async function handleDelete(row: ClassItem) {
  await ElMessageBox.confirm(`确定删除班级「${row.className}」吗？`, '提示', {
    type: 'warning'
  })
  try {
    await deleteClassApi(row.id)
    ElMessage.success('删除成功')
    fetchList()
  } catch {
    /* 错误已在拦截器处理 */
  }
}

onMounted(() => {
  fetchGrades()
  fetchList()
})
</script>

<style scoped>
.class-page {
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

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.grade-filter {
  width: 200px;
}

.data-table {
  flex: 1;
  border-radius: 8px;
  overflow: hidden;
}
</style>
