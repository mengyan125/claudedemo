<template>
  <div class="user-page">
    <!-- 工具栏 -->
    <div class="toolbar">
      <el-input
        v-model="keyword"
        placeholder="搜索用户名/姓名..."
        clearable
        class="search-input"
        @clear="handleSearch"
        @keyup.enter="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <div class="btn-group">
        <el-button type="primary" round @click="openCreateDialog">
          <el-icon><Plus /></el-icon>新增用户
        </el-button>
        <el-button round @click="openBatchDialog">
          <el-icon><Upload /></el-icon>批量导入
        </el-button>
      </div>
    </div>

    <!-- 用户表格 -->
    <el-table
      v-loading="loading"
      :data="userList"
      stripe
      class="user-table"
    >
      <el-table-column prop="username" label="用户名" width="180" />
      <el-table-column prop="realName" label="真实姓名" width="160" />
      <el-table-column label="用户类型" width="120">
        <template #default="{ row }">
          <el-tag :type="userTypeTag(row.userType)" size="small">
            {{ userTypeLabel(row.userType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="角色" width="200">
        <template #default="{ row }">
          {{ row.roles?.join('、') || '—' }}
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <span :class="['status-dot', row.status === 1 ? 'active' : 'disabled']">
            {{ row.status === 1 ? '正常' : '禁用' }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="200" />
      <el-table-column label="操作" min-width="120">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button
            v-if="row.status === 1"
            link type="danger"
            @click="handleDelete(row)"
          >删除</el-button>
          <el-button
            v-else
            link type="success"
            @click="handleToggleStatus(row)"
          >启用</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-bar">
      <span class="total-text">共 {{ total }} 条记录</span>
      <el-pagination
        v-model:current-page="pageNum"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="fetchUserList"
      />
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editingUser ? '编辑用户' : '新增用户'"
      width="480px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="80px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="formData.username"
            placeholder="请输入用户名"
            :disabled="!!editingUser"
          />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="formData.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item v-if="!editingUser" label="密码" prop="password">
          <el-input
            v-model="formData.password"
            type="password"
            placeholder="请输入密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="用户类型" prop="userType">
          <el-select v-model="formData.userType" placeholder="请选择用户类型">
            <el-option label="学生" value="student" />
            <el-option label="教师" value="teacher" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 批量导入弹窗 -->
    <el-dialog v-model="batchVisible" title="批量导入用户" width="480px">
      <p class="batch-tip">请上传 Excel 文件（.xlsx），包含列：用户名、真实姓名、密码、用户类型</p>
      <el-upload
        drag
        action=""
        :auto-upload="false"
        accept=".xlsx,.xls"
      >
        <el-icon :size="40" color="#C0C4CC"><Upload /></el-icon>
        <div class="el-upload__text">拖拽文件到此处，或<em>点击上传</em></div>
      </el-upload>
      <template #footer>
        <el-button @click="batchVisible = false">取消</el-button>
        <el-button type="primary">开始导入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { Search, Plus, Upload } from '@element-plus/icons-vue'
import {
  getUserListApi,
  createUserApi,
  updateUserApi,
  deleteUserApi,
  toggleUserStatusApi
} from '@/api/system'
import type { UserItem, CreateUserParams } from '@/api/system'

const keyword = ref('')
const loading = ref(false)
const userList = ref<UserItem[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

/* 弹窗状态 */
const dialogVisible = ref(false)
const batchVisible = ref(false)
const submitting = ref(false)
const editingUser = ref<UserItem | null>(null)
const formRef = ref<FormInstance>()
const formData = ref<CreateUserParams>({
  username: '',
  realName: '',
  password: '',
  userType: 'student'
})

const formRules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  userType: [{ required: true, message: '请选择用户类型', trigger: 'change' }]
}

/* 用户类型标签 */
function userTypeLabel(type: string) {
  const map: Record<string, string> = { student: '学生', teacher: '教师', admin: '管理员' }
  return map[type] || type
}

function userTypeTag(type: string) {
  const map: Record<string, string> = { student: 'success', teacher: 'warning', admin: '' }
  return map[type] || 'info'
}

/* 获取用户列表 */
async function fetchUserList() {
  loading.value = true
  try {
    const { data } = await getUserListApi({
      keyword: keyword.value,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    userList.value = data.data.list
    total.value = data.data.total
  } catch {
    /* 错误已在拦截器处理 */
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNum.value = 1
  fetchUserList()
}

/* 新增 */
function openCreateDialog() {
  editingUser.value = null
  formData.value = { username: '', realName: '', password: '', userType: 'student' }
  dialogVisible.value = true
}

/* 编辑 */
function handleEdit(row: UserItem) {
  editingUser.value = row
  formData.value = {
    username: row.username,
    realName: row.realName,
    password: '',
    userType: row.userType === 'admin' ? 'teacher' : row.userType
  }
  dialogVisible.value = true
}

/* 提交 */
async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (editingUser.value) {
      await updateUserApi(editingUser.value.id, {
        realName: formData.value.realName,
        userType: formData.value.userType
      })
      ElMessage.success('更新成功')
    } else {
      await createUserApi(formData.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchUserList()
  } catch {
    /* 错误已在拦截器处理 */
  } finally {
    submitting.value = false
  }
}

/* 删除 */
async function handleDelete(row: UserItem) {
  await ElMessageBox.confirm(`确定删除用户「${row.realName}」吗？`, '提示', {
    type: 'warning'
  })
  try {
    await deleteUserApi(row.id)
    ElMessage.success('删除成功')
    fetchUserList()
  } catch {
    /* 错误已在拦截器处理 */
  }
}

/* 启用/禁用 */
async function handleToggleStatus(row: UserItem) {
  const newStatus = row.status === 1 ? 0 : 1
  try {
    await toggleUserStatusApi(row.id, newStatus)
    ElMessage.success(newStatus === 1 ? '已启用' : '已禁用')
    fetchUserList()
  } catch {
    /* 错误已在拦截器处理 */
  }
}

/* 批量导入 */
function openBatchDialog() {
  batchVisible.value = true
}

onMounted(() => {
  fetchUserList()
})
</script>

<style scoped>
.user-page {
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

.search-input {
  width: 280px;
}

.btn-group {
  display: flex;
  gap: 12px;
}

.user-table {
  flex: 1;
  border-radius: 8px;
  overflow: hidden;
}

.status-dot {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
}

.status-dot::before {
  content: '';
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.status-dot.active { color: #67C23A; }
.status-dot.active::before { background: #67C23A; }
.status-dot.disabled { color: #F56C6C; }
.status-dot.disabled::before { background: #F56C6C; }

.pagination-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.total-text {
  font-size: 13px;
  color: #909399;
}

.batch-tip {
  font-size: 14px;
  color: #606266;
  margin: 0 0 16px 0;
}
</style>
