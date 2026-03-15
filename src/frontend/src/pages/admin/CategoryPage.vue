<template>
  <div class="category-page">
    <!-- 工具栏 -->
    <div class="toolbar">
      <span></span>
      <el-button class="btn-primary" round @click="openCreateDialog">
        <el-icon><Plus /></el-icon>新增类别
      </el-button>
    </div>

    <!-- 类别表格 -->
    <el-table v-loading="loading" :data="categoryList" stripe class="data-table">
      <el-table-column prop="name" label="类别名称" min-width="1" />
      <el-table-column label="关联任课教师" min-width="1">
        <template #default="{ row }">
          <el-tag :type="row.isTeachingRelated ? 'success' : 'info'" size="small">
            {{ row.isTeachingRelated ? '是' : '否' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="sortOrder" label="显示顺序" min-width="1" />
      <el-table-column label="状态" min-width="1">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
            {{ row.status === 1 ? '启用' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" min-width="1" align="center">
        <template #default="{ row }">
          <el-button
            text
            class="text-btn-primary"
            :disabled="row.feedbackCount > 0"
            @click="handleEdit(row)"
          >编辑</el-button>
          <el-button
            text
            :class="row.status === 1 ? 'text-btn-warning' : 'text-btn-success'"
            @click="handleToggleStatus(row)"
          >{{ row.status === 1 ? '停用' : '启用' }}</el-button>
          <el-button
            text
            class="text-btn-danger"
            :disabled="row.feedbackCount > 0"
            @click="handleDelete(row)"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editingItem ? '编辑类别' : '新增类别'"
      width="480px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
        label-position="left"
      >
        <el-form-item label="类别名称" prop="name">
          <el-input
            v-model="formData.name"
            placeholder="请输入类别名称"
            maxlength="10"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="关联任课教师" prop="isTeachingRelated">
          <el-radio-group v-model="formData.isTeachingRelated">
            <el-radio :value="true">是</el-radio>
            <el-radio :value="false">否</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="显示顺序" prop="sortOrder">
          <el-input-number v-model="formData.sortOrder" :min="1" :max="9" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="formData.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注"
            maxlength="200"
            show-word-limit
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
  getCategoryListApi,
  createCategoryApi,
  updateCategoryApi,
  deleteCategoryApi,
  toggleCategoryStatusApi
} from '@/api/admin'
import type { CategoryItem } from '@/api/admin'

const loading = ref(false)
const categoryList = ref<CategoryItem[]>([])

/* 弹窗状态 */
const dialogVisible = ref(false)
const submitting = ref(false)
const editingItem = ref<CategoryItem | null>(null)
const formRef = ref<FormInstance>()
const formData = ref({
  name: '',
  isTeachingRelated: false,
  sortOrder: 1,
  status: 1,
  remark: ''
})

const formRules: FormRules = {
  name: [
    { required: true, message: '请输入类别名称', trigger: 'blur' },
    { max: 10, message: '不超过10个字符', trigger: 'change' },
    { pattern: /^[\u4e00-\u9fa5a-zA-Z0-9]+$/, message: '仅支持汉字、字母、数字', trigger: 'change' }
  ],
  isTeachingRelated: [
    { required: true, message: '请选择是否关联任课教师', trigger: 'change' }
  ],
  sortOrder: [
    { required: true, message: '请输入显示顺序', trigger: 'change' }
  ]
}

/* 获取列表 */
async function fetchList() {
  loading.value = true
  try {
    const { data } = await getCategoryListApi()
    categoryList.value = data.data
  } catch {
    /* 错误已在拦截器处理 */
  } finally {
    loading.value = false
  }
}

/* 新增 */
function openCreateDialog() {
  editingItem.value = null
  formData.value = { name: '', isTeachingRelated: false, sortOrder: 1, status: 1, remark: '' }
  dialogVisible.value = true
}

/* 编辑 */
function handleEdit(row: CategoryItem) {
  if (row.feedbackCount > 0) {
    ElMessage.warning('该类别下已有反馈数据，不可编辑')
    return
  }
  editingItem.value = row
  formData.value = {
    name: row.name,
    isTeachingRelated: row.isTeachingRelated,
    sortOrder: row.sortOrder,
    status: row.status,
    remark: row.remark ?? ''
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
      await updateCategoryApi(editingItem.value.id, formData.value)
      ElMessage.success('更新成功')
    } else {
      await createCategoryApi(formData.value)
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

/* 启用/停用 */
async function handleToggleStatus(row: CategoryItem) {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 1 ? '启用' : '停用'
  await ElMessageBox.confirm(`确定${action}类别「${row.name}」吗？`, '提示', {
    type: 'warning'
  })
  try {
    await toggleCategoryStatusApi(row.id, newStatus)
    ElMessage.success(`${action}成功`)
    fetchList()
  } catch {
    /* 错误已在拦截器处理 */
  }
}

/* 删除 */
async function handleDelete(row: CategoryItem) {
  if (row.feedbackCount > 0) {
    ElMessage.warning('该类别下已有反馈数据，不可删除')
    return
  }
  await ElMessageBox.confirm(`确定删除类别「${row.name}」吗？`, '提示', {
    type: 'warning'
  })
  try {
    await deleteCategoryApi(row.id)
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
.category-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
  height: 100%;
  padding: 24px 40px;
  background: #f5f5f5;
  box-sizing: border-box;
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

.btn-primary {
  background-color: #2AABCB;
  border-color: #2AABCB;
  color: #fff;
}

.btn-primary:hover,
.btn-primary:focus {
  background-color: #24a0bf;
  border-color: #24a0bf;
  color: #fff;
}
:deep(.text-btn-primary.el-button) { color: #2AABCB; background: transparent; border: none; }
:deep(.text-btn-primary.el-button:hover) { color: #24a0bf; background: transparent; }
:deep(.text-btn-warning.el-button) { color: #E6A23C; background: transparent; border: none; }
:deep(.text-btn-warning.el-button:hover) { color: #d09a2e; background: transparent; }
:deep(.text-btn-success.el-button) { color: #67C23A; background: transparent; border: none; }
:deep(.text-btn-success.el-button:hover) { color: #529b2e; background: transparent; }
:deep(.text-btn-danger.el-button) { color: #F56C6C; background: transparent; border: none; }
:deep(.text-btn-danger.el-button:hover) { color: #f23c3c; background: transparent; }
</style>
