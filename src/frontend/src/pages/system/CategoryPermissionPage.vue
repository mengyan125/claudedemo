<template>
  <div class="category-permission-page">
    <!-- 表格 -->
    <el-table v-loading="loading" :data="permissionList" stripe class="data-table">
      <el-table-column prop="categoryName" label="类别名称" min-width="160" />
      <el-table-column label="负责管理员" min-width="300">
        <template #default="{ row }">
          <template v-if="getAdminNames(row.adminIds).length > 0">
            <el-tag
              v-for="name in getAdminNames(row.adminIds)"
              :key="name"
              size="small"
              class="admin-tag"
            >{{ name }}</el-tag>
          </template>
          <span v-else class="no-admin">暂无</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button text class="text-btn-primary" @click="openEditDialog(row)">
            编辑
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="`编辑类别权限 - ${editingItem?.categoryName ?? ''}`"
      width="520px"
      destroy-on-close
    >
      <!-- 搜索过滤 -->
      <el-input
        v-model="searchKeyword"
        placeholder="搜索管理员姓名"
        clearable
        class="search-input"
      />

      <!-- 已选管理员 -->
      <div v-if="selectedAdminIds.length > 0" class="selected-section">
        <div class="section-label">已选管理员（{{ selectedAdminIds.length }}人）</div>
        <div class="selected-tags">
          <el-tag
            v-for="id in selectedAdminIds"
            :key="id"
            closable
            size="default"
            @close="removeAdmin(id)"
          >{{ getUserName(id) }}</el-tag>
        </div>
      </div>

      <!-- 可选用户列表 -->
      <div class="user-list-section">
        <div class="section-label">可选管理员</div>
        <el-checkbox-group v-model="selectedAdminIds" class="user-checkbox-group">
          <div
            v-for="user in filteredUsers"
            :key="user.id"
            class="user-checkbox-item"
          >
            <el-checkbox :value="user.id">
              {{ user.realName }}
              <span class="user-type-label">（{{ formatUserType(user.userType) }}）</span>
            </el-checkbox>
          </div>
        </el-checkbox-group>
        <el-empty
          v-if="filteredUsers.length === 0"
          description="无匹配用户"
          :image-size="60"
        />
      </div>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getCategoryPermissionListApi,
  updateCategoryPermissionApi
} from '@/api/system'
import type { CategoryPermissionItem } from '@/api/system'
import { searchUsersApi } from '@/api/admin'
import type { UserSearchItem } from '@/api/admin'

const loading = ref(false)
const permissionList = ref<CategoryPermissionItem[]>([])

/* 全部非学生用户列表（用于映射名字和弹窗选择） */
const allUsers = ref<UserSearchItem[]>([])
/* 排除系统管理员（userType=admin），仅保留教师作为可选管理员 */
const selectableUsers = computed(() =>
  allUsers.value.filter(u => u.userType !== 'admin')
)
const userMap = computed(() => {
  const map = new Map<number, UserSearchItem>()
  allUsers.value.forEach(u => map.set(u.id, u))
  return map
})

/* 弹窗状态 */
const dialogVisible = ref(false)
const submitting = ref(false)
const editingItem = ref<CategoryPermissionItem | null>(null)
const selectedAdminIds = ref<number[]>([])
const searchKeyword = ref('')

/* 过滤后的用户列表 */
const filteredUsers = computed(() => {
  const keyword = searchKeyword.value.trim().toLowerCase()
  if (!keyword) return selectableUsers.value
  return selectableUsers.value.filter(u =>
    u.realName.toLowerCase().includes(keyword) ||
    u.username.toLowerCase().includes(keyword)
  )
})

/* 获取管理员姓名列表 */
function getAdminNames(adminIds: number[]): string[] {
  if (!adminIds || adminIds.length === 0) return []
  return adminIds
    .map(id => userMap.value.get(id)?.realName)
    .filter((name): name is string => !!name)
}

/* 获取单个用户姓名 */
function getUserName(id: number): string {
  return userMap.value.get(id)?.realName ?? `用户${id}`
}

/* 格式化用户类型 */
function formatUserType(type: string): string {
  const map: Record<string, string> = {
    admin: '管理员',
    teacher: '教师'
  }
  return map[type] ?? type
}

/* 移除已选管理员 */
function removeAdmin(id: number) {
  selectedAdminIds.value = selectedAdminIds.value.filter(i => i !== id)
}

/* 获取权限列表 */
async function fetchPermissionList() {
  loading.value = true
  try {
    const { data } = await getCategoryPermissionListApi()
    permissionList.value = data.data
  } catch {
    /* 错误已在拦截器处理 */
  } finally {
    loading.value = false
  }
}

/* 获取全部非学生用户 */
async function fetchAllUsers() {
  try {
    const { data } = await searchUsersApi('')
    allUsers.value = data.data
  } catch {
    /* 错误已在拦截器处理 */
  }
}

/* 打开编辑弹窗 */
function openEditDialog(row: CategoryPermissionItem) {
  editingItem.value = row
  selectedAdminIds.value = [...(row.adminIds || [])]
  searchKeyword.value = ''
  dialogVisible.value = true
}

/* 保存 */
async function handleSave() {
  if (!editingItem.value) return
  submitting.value = true
  try {
    await updateCategoryPermissionApi(editingItem.value.categoryId, selectedAdminIds.value)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchPermissionList()
  } catch {
    /* 错误已在拦截器处理 */
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchPermissionList()
  fetchAllUsers()
})
</script>

<style scoped>
.category-permission-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
  height: 100%;
  padding: 24px 40px;
  background: #f5f5f5;
  box-sizing: border-box;
}

.data-table {
  flex: 1;
  border-radius: 8px;
  overflow: hidden;
}

.admin-tag {
  margin-right: 6px;
  margin-bottom: 4px;
}

.no-admin {
  color: #999;
  font-size: 13px;
}

.search-input {
  margin-bottom: 16px;
}

.selected-section {
  margin-bottom: 16px;
}

.section-label {
  font-size: 13px;
  color: #606266;
  margin-bottom: 8px;
  font-weight: 500;
}

.selected-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.user-list-section {
  max-height: 320px;
  overflow-y: auto;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 12px;
}

.user-checkbox-group {
  display: flex;
  flex-direction: column;
}

.user-checkbox-item {
  padding: 6px 0;
  border-bottom: 1px solid #f5f5f5;
}

.user-checkbox-item:last-child {
  border-bottom: none;
}

.user-type-label {
  color: #909399;
  font-size: 12px;
}

:deep(.text-btn-primary.el-button) { color: #2AABCB; background: transparent; border: none; }
:deep(.text-btn-primary.el-button:hover) { color: #24a0bf; background: transparent; }
</style>
