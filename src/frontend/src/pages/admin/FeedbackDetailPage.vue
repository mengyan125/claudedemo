<template>
  <div v-loading="loading" class="detail-page">
    <div class="breadcrumb">
      <span class="breadcrumb-link" @click="router.push('/admin/feedback')">反馈查看</span>
      <span class="breadcrumb-sep">&gt;</span>
      <span>查看详情</span>
    </div>

    <div v-if="detail" class="detail-card">
      <!-- 基本信息 -->
      <div class="info-row">
        <span class="info-item">反馈类别：{{ detail.categoryName }}</span>
        <span class="info-item">年级：{{ detail.gradeName }}</span>
        <span class="info-item">班级：{{ detail.className }}</span>
        <span v-if="detail.teacherName" class="info-item">反馈对象：{{ detail.teacherName }}</span>
        <span class="info-item">反馈人：{{ detail.isAnonymous ? '匿名' : detail.studentName }}</span>
        <span class="info-item">反馈时间：{{ detail.createTime }}</span>
      </div>

      <!-- 反馈主题 -->
      <div class="section">
        <div class="section-header">
          <h3 class="section-title">反馈主题：</h3>
          <div class="status-area">
            <span v-if="detail.status === 'replied'" class="status-tag replied">☺ 已回复</span>
            <span v-else class="status-tag pending">☺ 未回复</span>
            <span class="fav-tag">❤ 已收藏</span>
          </div>
        </div>
        <p class="section-content">{{ detail.title }}</p>
      </div>

      <!-- 反馈内容 -->
      <div class="section">
        <h3 class="section-title">反馈内容：</h3>
        <p class="section-content">{{ detail.content }}</p>
        <div v-for="att in detail.attachments" :key="att.id" class="attachment-item">
          <div class="att-icon" />
          <span class="att-name">{{ att.fileName }}</span>
          <a class="att-link" href="javascript:void(0)">下载</a>
          <a class="att-link" href="javascript:void(0)">预览</a>
        </div>
      </div>

      <!-- Tab切换 -->
      <div class="tab-bar">
        <div class="tab-active">反馈回复</div>
        <div class="tab-inactive">备注提醒</div>
      </div>

      <!-- 回复区域 -->
      <div class="reply-section">
        <el-input
          v-model="replyContent"
          type="textarea"
          :rows="5"
          placeholder="同学你好："
          maxlength="500"
          show-word-limit
        />
        <div class="quick-reply-row">
          <el-popover placement="top" :width="320" trigger="click">
            <template #reference>
              <span class="quick-reply-btn">快捷回复</span>
            </template>
            <div class="quick-reply-list">
              <div
                v-for="qr in quickReplies" :key="qr.id"
                class="quick-reply-item"
                @click="replyContent = qr.content"
              >{{ qr.content }}</div>
            </div>
          </el-popover>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="action-buttons">
        <el-button size="large" round @click="router.push('/admin/feedback')">返回</el-button>
        <el-button
          type="primary" size="large" round class="btn-primary"
          :loading="submitting" :disabled="!replyContent.trim()"
          @click="handleReply"
        >回复</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getAdminFeedbackDetailApi, adminReplyFeedbackApi, getQuickRepliesForReplyApi } from '@/api/admin'
import type { AdminFeedbackDetail } from '@/api/admin'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const submitting = ref(false)
const detail = ref<AdminFeedbackDetail | null>(null)
const replyContent = ref('')
const quickReplies = ref<Array<{ id: number; content: string }>>([])

async function fetchDetail() {
  const id = Number(route.params.id)
  loading.value = true
  try {
    const { data } = await getAdminFeedbackDetailApi(id)
    detail.value = data.data
  } catch { /* 错误已在拦截器处理 */ }
  finally { loading.value = false }
}

async function handleReply() {
  if (!replyContent.value.trim()) return
  submitting.value = true
  try {
    await adminReplyFeedbackApi(detail.value!.id, replyContent.value)
    ElMessage.success('回复成功')
    replyContent.value = ''
    fetchDetail()
  } catch { /* 错误已在拦截器处理 */ }
  finally { submitting.value = false }
}

onMounted(async () => {
  fetchDetail()
  try {
    const { data } = await getQuickRepliesForReplyApi()
    quickReplies.value = data.data
  } catch { /* 错误已在拦截器处理 */ }
})
</script>

<style scoped>
.detail-page { display: flex; flex-direction: column; gap: 16px; height: 100%; }

.breadcrumb { font-size: 13px; color: #666; }
.breadcrumb-link { cursor: pointer; }
.breadcrumb-link:hover { color: #2AABCB; }
.breadcrumb-sep { margin: 0 4px; }

.detail-card {
  background: #fff; border-radius: 8px; border: 1px solid #e8e8e8;
  padding: 28px 36px; display: flex; flex-direction: column; gap: 20px; flex: 1;
}

.info-row { display: flex; align-items: center; gap: 28px; flex-wrap: wrap; }
.info-item { font-size: 13px; color: #333; white-space: nowrap; }

.section { display: flex; flex-direction: column; gap: 10px; }
.section-header { display: flex; justify-content: space-between; align-items: center; }
.section-title { font-size: 16px; font-weight: 700; color: #333; margin: 0; }
.section-content { font-size: 14px; color: #333; margin: 0; padding-left: 16px; line-height: 1.8; }

.status-area { display: flex; gap: 16px; }
.status-tag { font-size: 13px; }
.status-tag.replied { color: #2AABCB; }
.status-tag.pending { color: #E6A23C; }
.fav-tag { font-size: 13px; color: #FF4D4F; cursor: pointer; }

.attachment-item { display: flex; align-items: center; gap: 12px; padding: 8px 16px; }
.att-icon { width: 32px; height: 32px; background: #e8f4fd; border-radius: 4px; }
.att-name { font-size: 13px; color: #333; }
.att-link { font-size: 13px; color: #2AABCB; text-decoration: none; cursor: pointer; }
.att-link:hover { text-decoration: underline; }

.tab-bar { display: flex; }
.tab-active {
  width: 140px; height: 40px; display: flex; align-items: center; justify-content: center;
  background: #2AABCB; color: #fff; font-size: 14px; font-weight: 600;
  border-radius: 6px 6px 0 0; cursor: pointer;
}
.tab-inactive {
  width: 140px; height: 40px; display: flex; align-items: center; justify-content: center;
  background: #f5f5f5; color: #666; font-size: 14px;
  border-radius: 6px 6px 0 0; border: 1px solid #dcdfe6; border-bottom: none; cursor: pointer;
}

.reply-section { display: flex; flex-direction: column; gap: 4px; }
.quick-reply-row { display: flex; justify-content: flex-end; }
.quick-reply-btn { font-size: 13px; color: #2AABCB; cursor: pointer; }
.quick-reply-btn:hover { text-decoration: underline; }

.quick-reply-list { display: flex; flex-direction: column; gap: 8px; }
.quick-reply-item { font-size: 13px; color: #333; padding: 8px 12px; border-radius: 4px; cursor: pointer; line-height: 1.6; }
.quick-reply-item:hover { background: #f5f5f5; }

.action-buttons { display: flex; justify-content: center; gap: 20px; }
.btn-primary { background-color: #2AABCB; border-color: #2AABCB; }
.btn-primary:hover, .btn-primary:focus { background-color: #24a0bf; border-color: #24a0bf; }
</style>
