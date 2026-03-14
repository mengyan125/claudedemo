<template>
  <div class="quick-reply-page">
    <!-- 工具栏 -->
    <div class="toolbar">
      <span class="page-title">快捷回复列表</span>
      <el-button class="btn-primary" round @click="openCreateDialog">
        <el-icon><Plus /></el-icon>新增快捷回复
      </el-button>
    </div>

    <!-- 快捷回复表格 -->
    <el-table v-loading="loading" :data="replyList" stripe class="data-table">
      <el-table-column prop="content" label="回复内容" min-width="300" show-overflow-tooltip />
      <el-table-column prop="sortOrder" label="排序" width="80" />
      <el-table-column prop="createUserName" label="创建人" width="120" />
      <el-table-column label="操作" width="140">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editingItem ? '编辑快捷回复' : '新增快捷回复'"
      width="520px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="80px"
      >
        <el-form-item label="回复内容" prop="content">
          <el-input
            v-model="formData.content"
            type="textarea"
            :rows="4"
            placeholder="请输入快捷回复内容"
            maxlength="500"
            show-word-limit
          />
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
  getQuickReplyListApi,
  createQuickReplyApi,
  updateQuickReplyApi,
  deleteQuickReplyApi
} from '@/api/admin'
import type { QuickReplyItem } from '@/api/admin'

const loading = ref(false)
const replyList = ref<QuickReplyItem[]>([])

/* 弹窗状态 */
const dialogVisible = ref(false)
const submitting = ref(false)
const editingItem = ref<QuickReplyItem | null>(null)
const formRef = ref<FormInstance>()
const formData = ref({
  content: '',
  sortOrder: 1
})

const formRules: FormRules = {
  content: [
    { required: true, message: '请输入回复内容', trigger: 'blur' },
    { max: 500, message: '不超过500个字符', trigger: 'change' }
  ],
  sortOrder: [
    { required: true, message: '请输入排序', trigger: 'change' }
  ]
}

/* 获取列表 */
async function fetchList() {
  loading.value = true
  try {
    const { data } = await getQuickReplyListApi()
    replyList.value = data.data
  } catch {
    /* 错误已在拦截器处理 */
  } finally {
    loading.value = false
  }
}

/* 新增 */
function openCreateDialog() {
  editingItem.value = null
  formData.value = { content: '', sortOrder: 1 }
  dialogVisible.value = true
}

/* 编辑 */
function handleEdit(row: QuickReplyItem) {
  editingItem.value = row
  formData.value = {
    content: row.content,
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
      await updateQuickReplyApi(editingItem.value.id, formData.value)
      ElMessage.success('更新成功')
    } else {
      await createQuickReplyApi(formData.value)
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
async function handleDelete(row: QuickReplyItem) {
  await ElMessageBox.confirm('确定删除该快捷回复吗？', '提示', {
    type: 'warning'
  })
  try {
    await deleteQuickReplyApi(row.id)
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
.quick-reply-page {
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
</style>
