<template>
  <div v-loading="loading" class="edit-page">
    <!-- 表单卡片 -->
    <div class="form-card">
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
        label-position="left"
        size="large"
      >
        <!-- 第一行：类别 + 任课老师 + 反馈对象 -->
        <div class="form-row-inline">
          <el-form-item label="反馈类别" prop="categoryId" class="inline-item">
            <el-select v-model="formData.categoryId" placeholder="请选择反馈类别" @change="handleCategoryChange">
              <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
            </el-select>
          </el-form-item>

          <template v-if="currentCategory?.isTeachingRelated">
            <div class="radio-inline">
              <el-checkbox v-model="formData.isTeachingRelated">与任课老师相关</el-checkbox>
            </div>
            <el-form-item v-if="formData.isTeachingRelated" label="反馈对象" prop="teacherId" class="inline-item">
              <el-select v-model="formData.teacherId" placeholder="请选择任课教师">
                <el-option v-for="t in teachers" :key="t.id" :label="`${t.subject}-${t.name}`" :value="t.id" />
              </el-select>
            </el-form-item>
          </template>
        </div>

        <!-- 反馈主题 -->
        <el-form-item label="反馈主题" prop="title">
          <el-input v-model="formData.title" placeholder="请输入反馈主题" maxlength="20" show-word-limit />
        </el-form-item>

        <!-- 反馈内容 -->
        <el-form-item label="反馈内容" prop="content">
          <el-input
            v-model="formData.content"
            type="textarea"
            :rows="6"
            placeholder="请输入你的反馈内容"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <!-- 匿名反馈 -->
        <el-form-item>
          <el-checkbox v-model="formData.isAnonymous">
            匿名反馈
          </el-checkbox>
          <span class="hint-text">（选择匿名反馈后，不显示反馈人姓名）</span>
        </el-form-item>

        <!-- 附件上传 -->
        <el-form-item label="附件">
          <div class="upload-area">
            <el-upload
              :auto-upload="false"
              :file-list="fileList"
              :on-change="handleFileChange"
              :on-remove="handleFileRemove"
              :limit="9"
              accept=".jpg,.jpeg,.png,.gif,.mp4,.avi,.mov"
              multiple
            >
              <el-button class="btn-upload">点击上传</el-button>
            </el-upload>
            <p class="upload-hint">只能上传图片和视频，且不超过25M</p>
          </div>
        </el-form-item>

        <!-- 操作按钮 -->
        <div class="form-actions">
          <el-button size="large" round @click="handleSave">暂存</el-button>
          <el-button type="primary" size="large" round class="btn-primary" :loading="submitting" @click="handleSubmit">
            提交
          </el-button>
        </div>
      </el-form>
    </div>

    <!-- 提交成功遮罩 -->
    <div v-if="showSuccess" class="success-overlay" @click="showSuccess = false">
      <div class="success-banner">
        你的反馈我们收到啦，我们将尽快处理！
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules, UploadFile } from 'element-plus'
import { CircleCheckFilled } from '@element-plus/icons-vue'
import {
  getStudentCategoriesApi,
  getStudentTeachersApi,
  getFeedbackDetailApi,
  submitFeedbackApi,
  uploadFileApi
} from '@/api/student'
import type { StudentCategory, TeacherOption } from '@/api/student'

const route = useRoute()
const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)
const submitting = ref(false)
const showSuccess = ref(false)
const feedbackId = ref<number>(0)

const categories = ref<StudentCategory[]>([])
const teachers = ref<TeacherOption[]>([])
const fileList = ref<UploadFile[]>([])

const formData = reactive({
  categoryId: undefined as number | undefined,
  isTeachingRelated: true,
  teacherId: undefined as number | undefined,
  title: '',
  content: '',
  isAnonymous: false
})

const currentCategory = computed(() =>
  categories.value.find(c => c.id === formData.categoryId)
)

const formRules: FormRules = {
  categoryId: [{ required: true, message: '请选择反馈类别', trigger: 'change' }],
  teacherId: [{ required: true, message: '请选择反馈对象', trigger: 'change' }],
  title: [
    { required: true, message: '请输入反馈主题', trigger: 'blur' },
    { max: 20, message: '不超过20个字符', trigger: 'change' }
  ],
  content: [
    { required: true, message: '请输入反馈内容', trigger: 'blur' },
    { max: 500, message: '不超过500个字符', trigger: 'change' }
  ]
}

function handleCategoryChange() {
  formData.teacherId = undefined
  formData.isTeachingRelated = true
}

function handleFileChange(_file: UploadFile, newFileList: UploadFile[]) {
  fileList.value = newFileList
}

function handleFileRemove(_file: UploadFile, newFileList: UploadFile[]) {
  fileList.value = newFileList
}

async function uploadAllFiles(): Promise<number[]> {
  const ids: number[] = []
  for (const file of fileList.value) {
    if (file.raw) {
      const { data } = await uploadFileApi(file.raw)
      ids.push(data.data.id)
    }
  }
  return ids
}

/* 暂存 */
async function handleSave() {
  submitting.value = true
  try {
    const attachmentIds = await uploadAllFiles()
    await submitFeedbackApi({
      id: feedbackId.value,
      categoryId: formData.categoryId!,
      isTeachingRelated: currentCategory.value?.isTeachingRelated ?? false,
      teacherId: formData.teacherId ?? null,
      title: formData.title,
      content: formData.content,
      isAnonymous: formData.isAnonymous,
      status: 'draft',
      attachmentIds
    })
    ElMessage.success('暂存成功')
    router.push('/student/tracking')
  } catch {
    /* 错误已在拦截器处理 */
  } finally {
    submitting.value = false
  }
}

/* 提交 */
async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const attachmentIds = await uploadAllFiles()
    await submitFeedbackApi({
      id: feedbackId.value,
      categoryId: formData.categoryId!,
      isTeachingRelated: currentCategory.value?.isTeachingRelated ?? false,
      teacherId: formData.teacherId ?? null,
      title: formData.title,
      content: formData.content,
      isAnonymous: formData.isAnonymous,
      status: 'submitted',
      attachmentIds
    })
    showSuccess.value = true
    setTimeout(() => {
      showSuccess.value = false
      router.push('/student/tracking')
    }, 2000)
  } catch {
    /* 错误已在拦截器处理 */
  } finally {
    submitting.value = false
  }
}

/* 加载暂存数据 */
onMounted(async () => {
  feedbackId.value = Number(route.params.id)
  loading.value = true
  try {
    const [catRes, teacherRes, detailRes] = await Promise.all([
      getStudentCategoriesApi(),
      getStudentTeachersApi(),
      getFeedbackDetailApi(feedbackId.value)
    ])
    categories.value = catRes.data.data
    teachers.value = teacherRes.data.data

    const d = detailRes.data.data
    formData.categoryId = d.categoryId
    formData.isTeachingRelated = d.isTeachingRelated
    formData.teacherId = d.teacherId ?? undefined
    formData.title = d.title
    formData.content = d.content
    formData.isAnonymous = d.isAnonymous
  } catch {
    /* 错误已在拦截器处理 */
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.edit-page {
  display: flex;
  justify-content: center;
  padding: 32px 80px;
  height: 100%;
  box-sizing: border-box;
  position: relative;
}

.form-card {
  width: 100%;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #e8e8e8;
  padding: 40px 48px;
}

.form-row-inline {
  display: flex;
  align-items: flex-start;
  gap: 24px;
  margin-bottom: 20px;
}

.inline-item {
  margin-bottom: 0;
}

.inline-item :deep(.el-select) {
  width: 200px;
}

.radio-inline {
  display: flex;
  align-items: center;
  gap: 6px;
  height: 40px;
  flex-shrink: 0;
}

.radio-text {
  font-size: 14px;
  color: #333;
  white-space: nowrap;
}

.hint-text {
  font-size: 13px;
  color: #999;
  margin-left: 8px;
}

.upload-area {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.btn-upload {
  background-color: #2AABCB;
  border-color: #2AABCB;
  color: #fff;
  border-radius: 20px;
}

.btn-upload:hover,
.btn-upload:focus {
  background-color: #24a0bf;
  border-color: #24a0bf;
  color: #fff;
}

.upload-hint {
  font-size: 12px;
  color: #999;
  margin: 0;
}

.form-actions {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 12px;
}

.btn-primary {
  background-color: #2AABCB;
  border-color: #2AABCB;
}

.btn-primary:hover,
.btn-primary:focus {
  background-color: #24a0bf;
  border-color: #24a0bf;
}

.success-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.2);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 100;
  cursor: pointer;
}

.success-banner {
  background: #2AABCB;
  color: #fff;
  font-size: 18px;
  font-weight: 600;
  padding: 16px 48px;
  border-radius: 8px;
}
</style>
