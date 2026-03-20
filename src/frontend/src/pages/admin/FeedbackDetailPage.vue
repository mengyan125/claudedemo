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
            <span
              class="fav-tag"
              :class="{ 'fav-tag-inactive': !detail.isFavorited }"
              @click="handleToggleFavorite"
            >
              {{ detail.isFavorited ? '❤ 已收藏' : '♡ 收藏' }}
            </span>
          </div>
        </div>
        <p class="section-content">{{ detail.title }}</p>
      </div>

      <!-- 反馈内容 -->
      <div class="section">
        <h3 class="section-title">反馈内容：</h3>
        <p class="section-content">{{ detail.content }}</p>
        <div v-for="att in detail.attachments" :key="att.id" class="attachment-item">
          <span class="att-name">{{ att.fileName }}</span>
          <a class="att-link" :href="att.fileUrl" download>下载</a>
          <a class="att-link" @click="previewAttachment(att)">预览</a>
        </div>
      </div>

      <!-- Tab切换 -->
      <div class="tab-bar">
        <div :class="activeTab === 'reply' ? 'tab-active' : 'tab-inactive'" @click="activeTab = 'reply'">反馈回复</div>
        <div :class="activeTab === 'reminder' ? 'tab-active' : 'tab-inactive'" @click="switchToReminder">备注提醒</div>
      </div>

      <!-- 反馈回复Tab -->
      <template v-if="activeTab === 'reply'">
        <!-- 回复列表 -->
        <div class="reply-list-section">
          <div v-if="!detail.replies || detail.replies.length === 0" class="no-reply">暂无回复</div>
          <div v-for="reply in detail.replies" :key="reply.id" class="reply-item">
            <p class="reply-content">{{ reply.content }}</p>
            <div class="reply-footer">
              <span class="reply-info">{{ reply.replyUserName }} {{ formatUserType(reply.userType) }}</span>
              <span class="reply-info">{{ reply.createTime }}</span>
            </div>
          </div>
        </div>

        <!-- 回复输入区域 -->
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
            <span class="quick-reply-btn" @click="fillActiveQuickReply">快捷回复</span>
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
      </template>

      <!-- 备注提醒Tab -->
      <template v-if="activeTab === 'reminder'">
        <!-- 提醒列表 -->
        <div class="reminder-list-section">
          <div v-if="reminderList.length === 0" class="no-reply">暂无备注提醒</div>
          <div v-for="r in reminderList" :key="r.id" class="reply-item">
            <p class="reply-content">{{ r.content }}</p>
            <div class="reminder-meta">
              <span>{{ r.senderName }}</span>
              <span>{{ r.createTime }}</span>
            </div>
          </div>
        </div>

        <!-- 发送新提醒 -->
        <div class="reply-section">
          <el-input
            v-model="reminderContent"
            type="textarea"
            :rows="4"
            placeholder="请输入备注内容"
            maxlength="500"
            show-word-limit
          />
          <div class="reminder-receivers">
            <el-button size="small" @click="openReceiverDialog">选择接收人</el-button>
            <span v-if="selectedReceiverIds.length === 0" class="receiver-hint">不选则全员可见</span>
          </div>
          <div v-if="selectedReceiverIds.length > 0" class="selected-tags">
            <el-tag
              v-for="uid in selectedReceiverIds"
              :key="uid"
              closable
              size="small"
              @close="removeReceiver(uid)"
            >{{ getReceiverName(uid) }}</el-tag>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="action-buttons">
          <el-button size="large" round @click="router.push('/admin/feedback')">返回</el-button>
          <el-button
            type="primary" size="large" round class="btn-primary"
            :loading="sendingReminder" :disabled="!reminderContent.trim()"
            @click="handleSendReminder"
          >发送提醒</el-button>
        </div>
      </template>
    </div>

    <!-- 选择接收人弹窗 -->
    <el-dialog v-model="receiverDialogVisible" title="选择接收人" width="680px" destroy-on-close>
      <el-transfer
        v-model="tempSelectedIds"
        :data="allUsers"
        :titles="['可选人员', '已选人员']"
        filterable
        filter-placeholder="搜索姓名"
      />
      <template #footer>
        <el-button @click="receiverDialogVisible = false">取消</el-button>
        <el-button type="primary" class="btn-primary" @click="confirmReceivers">确定</el-button>
      </template>
    </el-dialog>

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
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  getAdminFeedbackDetailApi,
  adminReplyFeedbackApi,
  getQuickRepliesForReplyApi,
  toggleFavoriteApi,
  sendReminderApi,
  getReminderListApi,
  searchUsersApi
} from '@/api/admin'
import type { AdminFeedbackDetail, ReminderItem, UserSearchItem } from '@/api/admin'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const submitting = ref(false)
const detail = ref<AdminFeedbackDetail | null>(null)
const replyContent = ref('')
const quickReplies = ref<Array<{ id: number; content: string; isActive?: boolean }>>([])
const activeTab = ref<'reply' | 'reminder'>('reply')

/* 附件预览 */
const previewVisible = ref(false)
const previewUrl = ref('')
const previewType = ref<'image' | 'video'>('image')
const IMAGE_EXTS = ['jpg', 'jpeg', 'png', 'gif']
const VIDEO_EXTS = ['mp4', 'avi', 'mov']

/* 备注提醒 */
const reminderList = ref<ReminderItem[]>([])
const reminderContent = ref('')
const selectedReceiverIds = ref<number[]>([])
const allUsers = ref<Array<{ key: number; label: string }>>([])
const allUsersRaw = ref<UserSearchItem[]>([])
const receiverDialogVisible = ref(false)
const tempSelectedIds = ref<number[]>([])
const sendingReminder = ref(false)

function formatUserType(type: string) {
  const map: Record<string, string> = { admin: '管理员', student: '学生', teacher: '教师' }
  return map[type] || type
}

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

async function handleToggleFavorite() {
  if (!detail.value) return
  try {
    await toggleFavoriteApi(detail.value.id)
    detail.value.isFavorited = !detail.value.isFavorited
    ElMessage.success(detail.value.isFavorited ? '已收藏' : '已取消收藏')
  } catch { /* 错误已在拦截器处理 */ }
}

/* 切换到备注提醒Tab时加载数据 */
async function switchToReminder() {
  activeTab.value = 'reminder'
  await fetchReminders()
}

async function fetchReminders() {
  try {
    const feedbackId = Number(route.params.id)
    const { data } = await getReminderListApi({ pageNum: 1, pageSize: 50 })
    reminderList.value = data.data.list.filter(item => item.feedbackId === feedbackId)
  } catch { /* 错误已在拦截器处理 */ }
}

async function fetchAllUsers() {
  try {
    const { data } = await searchUsersApi()
    allUsersRaw.value = data.data
    allUsers.value = data.data.map((u: UserSearchItem) => ({ key: u.id, label: u.realName }))
  } catch { /* 错误已在拦截器处理 */ }
}

function openReceiverDialog() {
  tempSelectedIds.value = [...selectedReceiverIds.value]
  if (allUsers.value.length === 0) {
    fetchAllUsers()
  }
  receiverDialogVisible.value = true
}

function confirmReceivers() {
  selectedReceiverIds.value = [...tempSelectedIds.value]
  receiverDialogVisible.value = false
}

function removeReceiver(uid: number) {
  selectedReceiverIds.value = selectedReceiverIds.value.filter(id => id !== uid)
}

function getReceiverName(uid: number) {
  const user = allUsersRaw.value.find(u => u.id === uid)
  return user ? user.realName : String(uid)
}

async function handleSendReminder() {
  if (!reminderContent.value.trim() || !detail.value) return
  sendingReminder.value = true
  try {
    await sendReminderApi(detail.value.id, {
      content: reminderContent.value,
      receiverIds: selectedReceiverIds.value.length > 0 ? selectedReceiverIds.value : undefined
    })
    ElMessage.success('提醒发送成功')
    reminderContent.value = ''
    selectedReceiverIds.value = []
    fetchReminders()
  } catch { /* 错误已在拦截器处理 */ }
  finally { sendingReminder.value = false }
}

/** 点击快捷回复，自动填充启用的那条内容 */
function fillActiveQuickReply() {
  const active = quickReplies.value.find(qr => qr.isActive)
  if (active) {
    replyContent.value = active.content
  } else {
    ElMessage.info('暂无启用的快捷回复')
  }
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
.fav-tag-inactive { color: #999; }

.attachment-item { display: flex; align-items: center; gap: 12px; padding: 8px 16px; }
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

.reply-list-section { display: flex; flex-direction: column; gap: 16px; }
.reminder-list-section { display: flex; flex-direction: column; gap: 16px; }
.reminder-meta { display: flex; gap: 16px; font-size: 12px; color: #999; }

.reply-item { padding: 12px 16px; background: #fafafa; border-radius: 6px; display: flex; flex-direction: column; gap: 8px; }
.reply-footer { display: flex; justify-content: space-between; align-items: center; }
.reply-info { font-size: 12px; color: #999; }
.reply-content { font-size: 14px; color: #333; margin: 0; line-height: 1.8; }
.no-reply { font-size: 14px; color: #999; padding-left: 16px; }

.reply-section { display: flex; flex-direction: column; gap: 8px; }
.quick-reply-row { display: flex; justify-content: flex-end; }
.quick-reply-btn { font-size: 13px; color: #2AABCB; cursor: pointer; }
.quick-reply-btn:hover { text-decoration: underline; }

.quick-reply-list { display: flex; flex-direction: column; gap: 8px; }
.quick-reply-item { font-size: 13px; color: #333; padding: 8px 12px; border-radius: 4px; cursor: pointer; line-height: 1.6; }
.quick-reply-item:hover { background: #f5f5f5; }

.reminder-receivers { display: flex; align-items: center; gap: 12px; margin-top: 8px; }
.receiver-hint { font-size: 12px; color: #999; }
.selected-tags { display: flex; flex-wrap: wrap; gap: 8px; margin-top: 8px; }

.action-buttons { display: flex; justify-content: center; gap: 20px; }
.btn-primary { background-color: #2AABCB; border-color: #2AABCB; }
.btn-primary:hover, .btn-primary:focus { background-color: #24a0bf; border-color: #24a0bf; }

.preview-container { display: flex; justify-content: center; align-items: center; }
.preview-image { max-width: 100%; max-height: 70vh; }
.preview-video { max-width: 100%; max-height: 70vh; }
</style>
