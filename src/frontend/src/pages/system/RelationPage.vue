<template>
  <div class="relation-page">
    <div class="main-area">
      <!-- 左侧：Tab + 树形导航 -->
      <div class="left-col">
        <div class="inner-tabs">
          <div
            :class="['inner-tab', { active: activeTab === 'student' }]"
            @click="activeTab = 'student'"
          >学生分班</div>
          <div
            :class="['inner-tab', { active: activeTab === 'teacher' }]"
            @click="activeTab = 'teacher'"
          >教师任课</div>
        </div>
        <div class="tree-panel">
          <div v-for="grade in gradeClassTree" :key="grade.id" class="tree-grade">
            <div class="grade-item" @click="toggleGrade(grade.id)">
              <span class="arrow">{{ expandedGrades.has(grade.id) ? '▼' : '▶' }}</span>
              <span class="grade-name">{{ grade.gradeName }}</span>
            </div>
            <template v-if="expandedGrades.has(grade.id)">
              <div
                v-for="cls in grade.classes"
                :key="cls.id"
                :class="['class-item', { active: selectedClassId === cls.id }]"
                @click="selectClass(cls)"
              >{{ cls.className }}</div>
            </template>
          </div>
        </div>
      </div>

      <!-- 右侧：列表面板 -->
      <div class="right-panel">
        <template v-if="selectedClass">
          <div class="right-header">
            <div class="title-group">
              <span class="title-text">{{ selectedGradeName }}{{ selectedClass.className }}</span>
              <span class="badge">{{ currentList.length }}人</span>
            </div>
            <el-button type="primary" size="small" @click="openAssignDialog">
              <el-icon><Plus /></el-icon>
              {{ activeTab === 'student' ? '分配学生' : '分配教师' }}
            </el-button>
          </div>

          <!-- 学生分班表格 -->
          <el-table
            v-if="activeTab === 'student'"
            v-loading="listLoading"
            :data="studentList"
            class="right-table"
          >
            <el-table-column prop="studentName" label="姓名" min-width="1" />
            <el-table-column prop="username" label="用户名" min-width="1" />
            <el-table-column prop="assignTime" label="分配时间" min-width="1" />
            <el-table-column label="操作" min-width="1" align="center">
              <template #default="{ row }">
                <el-button text class="text-btn-danger" @click="handleRemoveStudent(row)">移除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 教师任课表格 -->
          <el-table
            v-if="activeTab === 'teacher'"
            v-loading="listLoading"
            :data="teacherList"
            class="right-table"
          >
            <el-table-column prop="teacherName" label="姓名" min-width="1" />
            <el-table-column prop="username" label="用户名" min-width="1" />
            <el-table-column prop="subject" label="科目" min-width="1" />
            <el-table-column prop="assignTime" label="分配时间" min-width="1" />
            <el-table-column label="操作" min-width="1" align="center">
              <template #default="{ row }">
                <el-button text class="text-btn-danger" @click="handleRemoveTeacher(row)">移除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </template>

        <div v-else class="empty-tip">
          <el-empty description="请在左侧选择班级" />
        </div>
      </div>
    </div>

    <!-- 分配学生弹窗 -->
    <el-dialog v-model="assignStudentVisible" title="分配学生" width="480px" destroy-on-close>
      <el-input
        v-model="assignKeyword"
        placeholder="搜索学生姓名..."
        clearable
        class="assign-search"
      />
      <div class="assign-list">
        <el-checkbox-group v-model="selectedStudentIds">
          <div v-for="s in filteredUnassigned" :key="s.studentId" class="assign-item">
            <el-checkbox :value="s.studentId">{{ s.studentName }}（{{ s.username }}）</el-checkbox>
          </div>
        </el-checkbox-group>
        <el-empty v-if="filteredUnassigned.length === 0" description="暂无可分配学生" />
      </div>
      <template #footer>
        <el-button @click="assignStudentVisible = false">取消</el-button>
        <el-button type="primary" :loading="assigning" @click="handleAssignStudents">确定</el-button>
      </template>
    </el-dialog>

    <!-- 分配教师弹窗 -->
    <el-dialog v-model="assignTeacherVisible" title="分配教师" width="420px" destroy-on-close>
      <el-form ref="teacherFormRef" :model="teacherForm" :rules="teacherRules" label-width="70px">
        <el-form-item label="教师" prop="teacherId">
          <el-select v-model="teacherForm.teacherId" placeholder="请选择教师" style="width: 100%">
            <el-option
              v-for="t in teacherOptions"
              :key="t.id"
              :label="t.realName"
              :value="t.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="科目" prop="subject">
          <el-input v-model="teacherForm.subject" placeholder="请输入科目" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignTeacherVisible = false">取消</el-button>
        <el-button type="primary" :loading="assigning" @click="handleAssignTeacher">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  getGradeListApi,
  getClassListApi,
  getClassStudentsApi,
  getUnassignedStudentsApi,
  assignStudentsApi,
  removeStudentAssignmentApi,
  getClassTeachersApi,
  assignTeacherApi,
  removeTeacherAssignmentApi
} from '@/api/base'
import { getUserListApi } from '@/api/system'
import type {
  GradeItem,
  ClassItem,
  StudentAssignment,
  TeacherAssignment
} from '@/api/base'
import type { UserItem } from '@/api/system'

/* Tab 切换 */
const activeTab = ref<'student' | 'teacher'>('student')

/* 树形数据 */
interface GradeNode extends GradeItem {
  classes: ClassItem[]
}

const gradeClassTree = ref<GradeNode[]>([])
const expandedGrades = ref(new Set<number>())
const selectedClassId = ref<number | undefined>(undefined)
const selectedClass = ref<ClassItem | null>(null)
const selectedGradeName = ref('')

/* 右侧列表 */
const listLoading = ref(false)
const studentList = ref<StudentAssignment[]>([])
const teacherList = ref<TeacherAssignment[]>([])

const currentList = computed(() =>
  activeTab.value === 'student' ? studentList.value : teacherList.value
)

/* 分配学生弹窗 */
const assignStudentVisible = ref(false)
const assignKeyword = ref('')
const unassignedStudents = ref<StudentAssignment[]>([])
const selectedStudentIds = ref<number[]>([])
const assigning = ref(false)

const filteredUnassigned = computed(() => {
  if (!assignKeyword.value) return unassignedStudents.value
  const kw = assignKeyword.value.toLowerCase()
  return unassignedStudents.value.filter(
    s => s.studentName.toLowerCase().includes(kw) || s.username.toLowerCase().includes(kw)
  )
})

/* 分配教师弹窗 */
const assignTeacherVisible = ref(false)
const teacherFormRef = ref<FormInstance>()
const teacherForm = ref({ teacherId: undefined as number | undefined, subject: '' })
const teacherOptions = ref<UserItem[]>([])
const teacherRules: FormRules = {
  teacherId: [{ required: true, message: '请选择教师', trigger: 'change' }],
  subject: [{ required: true, message: '请输入科目', trigger: 'blur' }]
}

/* 加载树形数据 */
async function fetchTree() {
  try {
    const [gradesRes, classesRes] = await Promise.all([
      getGradeListApi(),
      getClassListApi()
    ])
    const grades = gradesRes.data.data
    const classes = classesRes.data.data
    gradeClassTree.value = grades.map(g => ({
      ...g,
      classes: classes.filter(c => c.gradeId === g.id)
    }))
    if (grades.length > 0) {
      expandedGrades.value.add(grades[0].id)
    }
  } catch {
    /* 错误已在拦截器处理 */
  }
}

function toggleGrade(gradeId: number) {
  if (expandedGrades.value.has(gradeId)) {
    expandedGrades.value.delete(gradeId)
  } else {
    expandedGrades.value.add(gradeId)
  }
}

function selectClass(cls: ClassItem) {
  selectedClassId.value = cls.id
  selectedClass.value = cls
  const grade = gradeClassTree.value.find(g => g.id === cls.gradeId)
  selectedGradeName.value = grade ? grade.gradeName : ''
  fetchListData()
}

/* 加载右侧列表 */
async function fetchListData() {
  if (!selectedClassId.value) return
  listLoading.value = true
  try {
    if (activeTab.value === 'student') {
      const { data } = await getClassStudentsApi(selectedClassId.value)
      studentList.value = data.data
    } else {
      const { data } = await getClassTeachersApi(selectedClassId.value)
      teacherList.value = data.data
    }
  } catch {
    /* 错误已在拦截器处理 */
  } finally {
    listLoading.value = false
  }
}

/* Tab 切换时重新加载 */
watch(activeTab, () => {
  if (selectedClassId.value) fetchListData()
})

/* 打开分配弹窗 */
async function openAssignDialog() {
  if (activeTab.value === 'student') {
    assignKeyword.value = ''
    selectedStudentIds.value = []
    assignStudentVisible.value = true
    try {
      const { data } = await getUnassignedStudentsApi()
      unassignedStudents.value = data.data
    } catch {
      /* 错误已在拦截器处理 */
    }
  } else {
    teacherForm.value = { teacherId: undefined, subject: '' }
    assignTeacherVisible.value = true
    try {
      const { data } = await getUserListApi({ pageNum: 1, pageSize: 999, keyword: '' })
      teacherOptions.value = data.data.list.filter(u =>
        u.userType === 'teacher' &&
        !(u.roles?.includes('SYSTEM_ADMIN') || u.roles?.includes('ROLE_ADMIN'))
      )
    } catch {
      /* 错误已在拦截器处理 */
    }
  }
}

/* 分配学生 */
async function handleAssignStudents() {
  if (selectedStudentIds.value.length === 0) {
    ElMessage.warning('请选择要分配的学生')
    return
  }
  assigning.value = true
  try {
    await assignStudentsApi(selectedClassId.value!, selectedStudentIds.value)
    ElMessage.success('分配成功')
    assignStudentVisible.value = false
    fetchListData()
  } catch {
    /* 错误已在拦截器处理 */
  } finally {
    assigning.value = false
  }
}

/* 移除学生 */
async function handleRemoveStudent(row: StudentAssignment) {
  await ElMessageBox.confirm(`确定移除学生「${row.studentName}」吗？`, '提示', {
    type: 'warning'
  })
  try {
    await removeStudentAssignmentApi(row.id)
    ElMessage.success('移除成功')
    fetchListData()
  } catch {
    /* 错误已在拦截器处理 */
  }
}

/* 分配教师 */
async function handleAssignTeacher() {
  const valid = await teacherFormRef.value?.validate().catch(() => false)
  if (!valid) return
  assigning.value = true
  try {
    await assignTeacherApi(selectedClassId.value!, {
      teacherId: teacherForm.value.teacherId!,
      subject: teacherForm.value.subject
    })
    ElMessage.success('分配成功')
    assignTeacherVisible.value = false
    fetchListData()
  } catch {
    /* 错误已在拦截器处理 */
  } finally {
    assigning.value = false
  }
}

/* 移除教师 */
async function handleRemoveTeacher(row: TeacherAssignment) {
  await ElMessageBox.confirm(
    `确定移除教师「${row.teacherName}」的${row.subject}任课吗？`,
    '提示',
    { type: 'warning' }
  )
  try {
    await removeTeacherAssignmentApi(row.id)
    ElMessage.success('移除成功')
    fetchListData()
  } catch {
    /* 错误已在拦截器处理 */
  }
}

onMounted(() => {
  fetchTree()
})
</script>

<style scoped>
.relation-page {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.main-area {
  display: flex;
  gap: 16px;
  height: 100%;
}

/* 左侧列 */
.left-col {
  width: 220px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
}

.inner-tabs {
  display: flex;
  background: #fff;
  border-radius: 8px 8px 0 0;
  border-bottom: 1px solid #E4E7ED;
}

.inner-tab {
  padding: 10px 16px;
  font-size: 13px;
  color: #909399;
  cursor: pointer;
  border-bottom: 2px solid transparent;
  transition: all 0.2s;
}

.inner-tab.active {
  color: #2AABCB;
  font-weight: 500;
  border-bottom-color: #2AABCB;
}

.tree-panel {
  flex: 1;
  background: #fff;
  border-radius: 0 0 8px 8px;
  border: 1px solid #E4E7ED;
  border-top: none;
  overflow-y: auto;
  padding: 8px 0;
}

.grade-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  cursor: pointer;
}

.grade-item:hover {
  background: #F5F7FA;
}

.arrow {
  font-size: 10px;
  color: #909399;
}

.grade-name {
  font-size: 13px;
  font-weight: 500;
  color: #303133;
}

.class-item {
  padding: 8px 12px 8px 30px;
  font-size: 13px;
  color: #606266;
  cursor: pointer;
  border-radius: 4px;
  margin: 0 4px;
}

.class-item:hover {
  background: #F5F7FA;
}

.class-item.active {
  background: #ECF5FF;
  color: #2AABCB;
  font-weight: 500;
}

/* 右侧面板 */
.right-panel {
  flex: 1;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #E4E7ED;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.right-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 20px;
  background: #FAFAFA;
  border-bottom: 1px solid #E4E7ED;
}

.title-group {
  display: flex;
  align-items: center;
  gap: 10px;
}

.title-text {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.badge {
  font-size: 12px;
  font-weight: 500;
  color: #4CAF50;
  background: #E8F5E9;
  border-radius: 10px;
  padding: 3px 10px;
}

.right-table {
  flex: 1;
}

.empty-tip {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 分配弹窗 */
.assign-search {
  margin-bottom: 12px;
}

.assign-list {
  max-height: 300px;
  overflow-y: auto;
}

.assign-item {
  padding: 6px 0;
}
.text-btn-primary { color: #2AABCB !important; background: transparent !important; }
.text-btn-primary:hover { color: #24a0bf !important; }
.text-btn-danger { color: #F56C6C !important; background: transparent !important; }
.text-btn-danger:hover { color: #f23c3c !important; }
.text-btn-success { color: #67C23A !important; background: transparent !important; }
.text-btn-success:hover { color: #529b2e !important; }
</style>
