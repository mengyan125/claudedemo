<template>
  <div class="login-container">
    <!-- 左侧品牌区域 -->
    <div class="login-brand">
      <div class="brand-content">
        <div class="brand-logo">
          <el-icon :size="28" color="#2AABCB"><ChatDotSquare /></el-icon>
          <span class="brand-title">学生反馈系统</span>
        </div>
        <p class="brand-slogan">倾听每一个声音，让教育更美好</p>
        <div class="brand-features">
          <div class="feature-item">
            <el-icon :size="18"><Lock /></el-icon>
            <span>匿名保护 — 安全表达你的想法</span>
          </div>
          <div class="feature-item">
            <el-icon :size="18"><Lightning /></el-icon>
            <span>即时响应 — 反馈处理及时透明</span>
          </div>
          <div class="feature-item">
            <el-icon :size="18"><TrendCharts /></el-icon>
            <span>数据驱动 — 为管理决策提供支持</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧登录表单 -->
    <div class="login-form-wrapper">
      <div class="login-form-content">
        <h2 class="form-title">欢迎登录</h2>
        <p class="form-desc">请输入您的账号和密码</p>

        <el-form
          ref="formRef"
          :model="loginForm"
          :rules="rules"
          size="large"
          @keyup.enter="handleLogin"
        >
          <el-form-item prop="username">
            <label class="form-label">用户名</label>
            <el-input
              v-model="loginForm.username"
              placeholder="请输入用户名"
              clearable
            />
          </el-form-item>

          <el-form-item prop="password">
            <label class="form-label">密码</label>
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              show-password
            />
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              class="login-btn"
              :loading="loading"
              @click="handleLogin"
            >
              登 录
            </el-button>
          </el-form-item>
        </el-form>

        <p class="form-footer">© 2026 学生反馈系统</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { ChatDotSquare, Lock, Lightning, TrendCharts } from '@element-plus/icons-vue'
import { loginApi } from '@/api/auth'
import { useUserStore } from '@/stores/user'
import type { UserType } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

/* 登录处理 */
async function handleLogin() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const { data } = await loginApi(loginForm)
    const result = data.data

    /* 保存 token 和用户信息 */
    userStore.setToken(result.token)
    userStore.setUserInfo({
      ...result.userInfo,
      userType: result.userInfo.userType as UserType
    })

    ElMessage.success('登录成功')

    /* 根据角色跳转 */
    if (result.userInfo.userType === 'student') {
      router.push('/student/feedback')
    } else {
      router.push('/admin/feedback')
    }
  } catch {
    /* 错误已在拦截器中处理 */
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  height: 100vh;
  width: 100vw;
}

/* 左侧品牌区域 */
.login-brand {
  flex: 1;
  background: linear-gradient(160deg, #154360 0%, #1A5276 30%, #2E86C1 70%, #5DADE2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px 48px;
}

.brand-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 28px;
}

.brand-logo {
  display: flex;
  align-items: center;
  gap: 12px;
}

.brand-logo .el-icon {
  background: #fff;
  border-radius: 12px;
  padding: 10px;
}

.brand-title {
  font-size: 28px;
  font-weight: 700;
  color: #fff;
  letter-spacing: -0.5px;
}

.brand-slogan {
  font-size: 18px;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.8);
  margin: 0;
}

.brand-features {
  display: flex;
  flex-direction: column;
  gap: 18px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  padding: 28px 24px;
  width: 420px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  color: rgba(255, 255, 255, 0.8);
  font-size: 14px;
}

/* 右侧登录表单 */
.login-form-wrapper {
  width: 520px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 60px 80px;
  background: #fff;
}

.login-form-content {
  width: 100%;
}

.form-title {
  font-size: 26px;
  font-weight: 600;
  color: #111;
  margin: 0 0 8px 0;
  letter-spacing: -0.5px;
}

.form-desc {
  font-size: 14px;
  color: #666;
  margin: 0 0 32px 0;
}

.form-label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #111;
  margin-bottom: 6px;
}

.el-form-item {
  margin-bottom: 20px;
}

.login-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 500;
  border-radius: 999px;
}

.form-footer {
  text-align: center;
  font-size: 12px;
  color: #999;
  margin-top: 24px;
}
</style>
