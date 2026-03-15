<template>
  <div class="role-page">
    <!-- 左栏：角色列表 -->
    <div class="role-sidebar">
      <h3 class="sidebar-title">角色列表</h3>
      <div class="role-cards">
        <el-card
          v-for="role in roleList"
          :key="role.id"
          :class="['role-card', { active: selectedRole?.id === role.id }]"
          shadow="hover"
          @click="selectRole(role)"
        >
          <div class="role-card-name">{{ role.roleName }}</div>
          <div class="role-card-code">{{ role.roleCode }}</div>
        </el-card>
      </div>
      <el-empty v-if="!roleLoading && roleList.length === 0" description="暂无角色" :image-size="64" />
    </div>

    <!-- 右栏：用户列表 -->
    <div class="role-content">
      <template v-if="selectedRole">
        <div class="content-header">
          <h3 class="content-title">{{ selectedRole.roleName }} — 用户列表</h3>
          <el-button type="primary" round @click="openAddDialog">
            <el-icon><Plus /></el-icon>添加用户
          </el-button>
        </div>

        <el-table
          v-loading="usersLoading"
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
          <el-table-column label="操作" min-width="100">
            <template #default="{ row }">
              <el-button type="danger" size="small" @click="handleRemove(row)">移除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-empty v-if="!usersLoading && userList.length === 0" description="该角色下暂无用户" />
      </template>
      <el-empty v-else description="请先选择一个角色" :image-size="120" />
    </div>

    <!-- 添加用户弹窗 -->
    <el-dialog
      v-model="addDialogVisible"
      title="添加用户"
      width="520px"
      destroy-on-close
    >
      <el-input
        v-model="searchKeyword"
        placeholder="输入用户名或姓名搜索..."
        clearable
        class="search-input"
        @keyup.enter="handleSearchUsers"
        @clear="handleSearchUsers"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
        <template #append>
          <el-button @click="handleSearchUsers">搜索</el-button>
        </template>
      </el-input>

      <el-table
        v-loading="searchLoading"
        :data="searchResults"
        stripe
        class="search-table"
        max-height="320"
      >
        <el-table-column prop="username" label="用户名" width="140" />
        <el-table-column prop="realName" label="真实姓名" width="140" />
        <el-table-column label="用户类型" width="100">
          <template #default="{ row }">
            <el-tag :type="userTypeTag(row.userType)" size="small">
              {{ userTypeLabel(row.userType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              :disabled="isAlreadyAssigned(row.id)"
              @click="handleAssign(row)"
            >
              {{ isAlreadyAssigned(row.id) ? '已添加' : '添加' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="searchDone && searchResults.length === 0" description="未找到匹配用户" :image-size="64" />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import {
  getRoleListApi,
  getRoleUsersApi,
  assignRoleApi,
  revokeRoleApi
} from '@/api/system'
import type { RoleItem, UserRoleItem } from '@/api/system'
import { searchUsersApi } from '@/api/admin'
import type { UserSearchItem } from '@/api/admin'

/* 角色列表 */
const roleList = ref<RoleItem[]>([])
const roleLoading = ref(false)
const selectedRole = ref<RoleItem | null>(null)

/* 用户列表 */
const userList = ref<UserRoleItem[]>([])
const usersLoading = ref(false)

/* 添加用户弹窗 */
const addDialogVisible = ref(false)
const searchKeyword = ref('')
const searchResults = ref<UserSearchItem[]>([])
const searchLoading = ref(false)
const searchDone = ref(false)

/* 已分配用户 ID 集合，用于判断重复 */
const assignedUserIds = computed(() => new Set(userList.value.map(u => u.userId)))

/* 用户类型工具函数 */
function userTypeLabel(type: string) {
  const map: Record<string, string> = { student: '学生', teacher: '教师', admin: '管理员' }
  return map[type] || type
}

function userTypeTag(type: string) {
  const map: Record<string, string> = { student: 'success', teacher: 'warning', admin: '' }
  return map[type] || 'info'
}

/* 获取角色列表 */
async function fetchRoleList() {
  roleLoading.value = true
  try {
    const { data } = await getRoleListApi()
    roleList.value = data.data
    /* 默认选中第一个角色 */
    if (roleList.value.length > 0 && !selectedRole.value) {
      selectRole(roleList.value[0])
    }
  } catch {
    /* 错误已在拦截器处理 */
  } finally {
    roleLoading.value = false
  }
}

/* 选中角色 */
function selectRole(role: RoleItem) {
  selectedRole.value = role
  fetchRoleUsers()
}

/* 获取角色下的用户列表 */
async function fetchRoleUsers() {
  if (!selectedRole.value) return
  usersLoading.value = true
  try {
    const { data } = await getRoleUsersApi(selectedRole.value.id)
    userList.value = data.data
  } catch {
    /* 错误已在拦截器处理 */
  } finally {
    usersLoading.value = false
  }
}

/* 判断用户是否已分配 */
function isAlreadyAssigned(userId: number): boolean {
  return assignedUserIds.value.has(userId)
}

/* 打开添加用户弹窗 */
function openAddDialog() {
  searchKeyword.value = ''
  searchResults.value = []
  searchDone.value = false
  addDialogVisible.value = true
}

/* 搜索用户 */
async function handleSearchUsers() {
  if (!searchKeyword.value.trim()) {
    searchResults.value = []
    searchDone.value = false
    return
  }
  searchLoading.value = true
  searchDone.value = false
  try {
    const { data } = await searchUsersApi(searchKeyword.value.trim())
    searchResults.value = data.data
  } catch {
    /* 错误已在拦截器处理 */
  } finally {
    searchLoading.value = false
    searchDone.value = true
  }
}

/* 添加用户到角色 */
async function handleAssign(user: UserSearchItem) {
  if (!selectedRole.value) return
  try {
    await assignRoleApi({ userId: user.id, roleId: selectedRole.value.id })
    ElMessage.success(`已将「${user.realName}」添加到「${selectedRole.value.roleName}」`)
    fetchRoleUsers()
  } catch {
    /* 错误已在拦截器处理 */
  }
}

/* 移除用户 */
async function handleRemove(row: UserRoleItem) {
  if (!selectedRole.value) return
  await ElMessageBox.confirm(
    `确定将「${row.realName}」从「${selectedRole.value.roleName}」中移除吗？`,
    '确认移除',
    { type: 'warning' }
  )
  try {
    await revokeRoleApi({ userId: row.userId, roleId: selectedRole.value.id })
    ElMessage.success('移除成功')
    fetchRoleUsers()
  } catch {
    /* 错误已在拦截器处理 */
  }
}

onMounted(() => {
  fetchRoleList()
})
</script>

<style scoped>
.role-page {
  display: flex;
  gap: 16px;
  height: 100%;
}

/* 左栏 */
.role-sidebar {
  width: 240px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.sidebar-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0;
  padding-bottom: 8px;
  border-bottom: 1px solid #EBEEF5;
}

.role-cards {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.role-card {
  cursor: pointer;
  transition: all 0.2s ease;
  border: 2px solid transparent;
}

.role-card:hover {
  border-color: #b3d8ff;
}

.role-card.active {
  border-color: #409EFF;
  background: #f0f7ff;
}

.role-card :deep(.el-card__body) {
  padding: 14px 16px;
}

.role-card-name {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.role-card-code {
  font-size: 12px;
  color: #909399;
}

/* 右栏 */
.role-content {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.content-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.user-table {
  flex: 1;
  border-radius: 8px;
  overflow: hidden;
}

/* 弹窗内搜索 */
.search-input {
  margin-bottom: 16px;
}

.search-table {
  border-radius: 8px;
  overflow: hidden;
}
</style>
