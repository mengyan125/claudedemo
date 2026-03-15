<template>
  <div v-loading="loading" class="detail-page">
    <!-- 面包屑 -->
    <div class="breadcrumb">
      <span class="breadcrumb-link" @click="router.push('/student/tracking')">反馈跟踪</span>
      <span class="breadcrumb-sep">&gt;</span>
      <span>查看详情</span>
    </div>

    <!-- 详情卡片 -->
    <div v-if="detail" class="detail-card">
      <!-- 基本信息行 -->
      <div class="info-row">
        <span class="info-item">反馈类别：{{ detail.categoryName }}</span>
        <span v-if="detail.teacherName" class="info-item">反馈对象：{{ detail.teacherName }}</span>
        <span class="info-item">反馈时间：{{ detail.createTime }}</span>
      </div>

      <!-- 反馈主题 -->
      <div class="section">
        <div class="section-header">
          <h3 class="section-title">反馈主题：</h3>
          <span v-if="detail.status === 'replied'" class="status-tag">
            <el-icon><ChatDotSquare /></el-icon>
            已回复
          </span>
          <span v-else-if="detail.status === 'submitted'" class="status-tag pending">
            待回复
          </span>
        </div>
        <p class="section-content">{{ detail.title }}。</p>
      </div>

      <!-- 反馈内容 -->
      <div class="section">
        <h3 class="section-title">反馈内容：</h3>
        <p class="section-content">{{ detail.content }}</p>
        <!-- 附件 -->
        <div v-for="att in detail.attachments" :key="att.id" class="attachment-item">
          <span class="att-name">{{ att.fileName }}</span>
          <a class="att-link" :href="att.fileUrl" download>下载</a>
          <a class="att-link" @click="previewAttachment(att)">预览</a>
        </div>
      </div>

      <!-- 分隔线 -->
      <div class="divider" />

      <!-- 反馈回复 -->
      <div class="section">
        <h3 class="section-title">反馈回复：</h3>
        <div v-if="detail.replies.length === 0" class="no-reply">暂无回复</div>
        <div v-for="reply in detail.replies" :key="reply.id" class="reply-item">
          <p class="reply-content">{{ reply.content }}</p>
          <span class="reply-time">{{ reply.createTime }}</span>
        </div>
      </div>

      <!-- 追加回复输入 -->
      <template v-if="canReply">
        <div class="reply-input-section">
          <el-input
            v-model="replyContent"
            type="textarea"
            :rows="3"
            placeholder="请输入回复内容"
            maxlength="200"
            show-word-limit
          />
        </div>

        <!-- 操作按钮 -->
        <div class="action-buttons">
          <el-button size="large" round @click="router.push('/student/tracking')">返回</el-button>
          <el-button
            type="primary"
            size="large"
            round
            class="btn-primary"
            :loading="submitting"
            :disabled="!replyContent.trim()"
            @click="handleReply"
          >
            提交
          </el-button>
        </div>
      </template>

      <template v-else>
        <div class="wait-reply-hint">等待管理员回复后可继续反馈</div>
        <div class="action-buttons">
          <el-button size="large" round @click="router.push('/student/tracking')">返回</el-button>
        </div>
      </template>
    </div>

    <!-- 附件预览弹窗 -->
    <el-dialog v-model="previewVisible" title="附件预览" width="70%" destroy-on-close>
      <div class="preview-container">
        <img v-if="previewType === 'image'" :src="previewUrl" class="preview-image" />
        <video v-else-if="previewType === 'video'" :src="previewUrl" controls class="preview-video" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ChatDotSquare } from '@element-plus/icons-vue'
import { getFeedbackDetailApi, addReplyApi } from '@/api/student'
import type { FeedbackDetail } from '@/api/student'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const submitting = ref(false)
const detail = ref<FeedbackDetail | null>(null)
const replyContent = ref('')

/* 交替回复：只有最后一条回复不是学生发的，学生才能回复 */
const canReply = computed(() => {
  if (!detail.value || !detail.value.replies || detail.value.replies.length === 0) return false
  const lastReply = detail.value.replies[detail.value.replies.length - 1]
  return lastReply.userType !== 'student'
})
const previewVisible = ref(false)
const previewUrl = ref('')
const previewType = ref<'image' | 'video'>('image')

const IMAGE_EXTS = ['jpg', 'jpeg', 'png', 'gif']
const VIDEO_EXTS = ['mp4', 'avi', 'mov']

function previewAttachment(att: { fileName: string; fileUrl: string; fileType?: string }) {
  const ext = (att.fileType || att.fileName.split('.').pop() || '').toLowerCase()
  if (IMAGE_EXTS.includes(ext)) {
    previewType.value = 'image'
  } else if (VIDEO_EXTS.includes(ext)) {
    previewType.value = 'video'
  } else {
    ElMessage.info('该文件类型暂不支持预览')
    return
  }
  previewUrl.value = att.fileUrl
  previewVisible.value = true
}

/* 获取详情 */
async function fetchDetail() {
  const id = Number(route.params.id)
  loading.value = true
  try {
    const { data } = await getFeedbackDetailApi(id)
    detail.value = data.data
  } catch {
    /* 错误已在拦截器处理 */
  } finally {
    loading.value = false
  }
}

/* 提交回复 */
async function handleReply() {
  if (!replyContent.value.trim()) return
  submitting.value = true
  try {
    await addReplyApi(detail.value!.id, replyContent.value)
    ElMessage.success('回复成功')
    replyContent.value = ''
    fetchDetail()
  } catch {
    /* 错误已在拦截器处理 */
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchDetail()
})
</script>

<style scoped>
.detail-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
  height: 100%;
}

.breadcrumb {
  font-size: 13px;
  color: #666;
}

.breadcrumb-link {
  cursor: pointer;
}

.breadcrumb-link:hover {
  color: #2AABCB;
}

.breadcrumb-sep {
  margin: 0 4px;
}

.detail-card {
  background: #fff;
  border-radius: 8px;
  border: 1px solid #e8e8e8;
  padding: 32px 40px;
  display: flex;
  flex-direction: column;
  gap: 24px;
  flex: 1;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 40px;
}

.info-item {
  font-size: 14px;
  color: #333;
}

.section {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section-title {
  font-size: 16px;
  font-weight: 700;
  color: #333;
  margin: 0;
}

.status-tag {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #2AABCB;
}

.status-tag.pending {
  color: #E6A23C;
}

.section-content {
  font-size: 14px;
  color: #333;
  margin: 0;
  padding-left: 16px;
  line-height: 1.8;
}

.attachment-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 16px;
}

.att-name {
  font-size: 13px;
  color: #333;
}

.att-link {
  font-size: 13px;
  color: #2AABCB;
  text-decoration: none;
  cursor: pointer;
}

.att-link:hover {
  text-decoration: underline;
}

.divider {
  height: 1px;
  background: #eee;
}

.reply-item {
  padding-left: 16px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.reply-content {
  font-size: 14px;
  color: #333;
  margin: 0;
  line-height: 1.8;
}

.reply-time {
  font-size: 12px;
  color: #999;
}

.no-reply {
  font-size: 14px;
  color: #999;
  padding-left: 16px;
}

.wait-reply-hint {
  font-size: 14px;
  color: #999;
  text-align: center;
  padding: 16px 0;
}

.reply-input-section {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 20px;
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

.preview-container { display: flex; justify-content: center; align-items: center; }
.preview-image { max-width: 100%; max-height: 70vh; }
.preview-video { max-width: 100%; max-height: 70vh; }
</style>
