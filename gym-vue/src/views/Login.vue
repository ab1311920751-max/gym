<template>
  <div class="login-page">
    <!-- 左侧形象区 -->
    <div class="login-left">
      <div class="left-overlay"></div>
      <div class="left-content">
        <div class="brand">
          <div class="brand-logo">
            <span class="logo-icon">🏋️</span>
          </div>
          <div class="brand-name">POWER FITNESS</div>
        </div>
        <h1 class="left-title">课程预约系统</h1>
        <p class="left-subtitle">解锁你的潜力，开启精准预约。</p>
      </div>
    </div>

    <!-- 右侧表单区 -->
    <div class="login-right">
      <div class="login-card">
        <el-tabs v-model="activeTab" class="auth-tabs" stretch>
          <!-- 登录 -->
          <el-tab-pane label="登 录" name="login">
            <el-form
              :model="loginForm"
              :rules="loginRules"
              ref="loginFormRef"
              size="large"
              hide-required-asterisk
            >
              <el-form-item prop="username">
                <el-input
                  v-model="loginForm.username"
                  placeholder="请输入账号"
                  :prefix-icon="User"
                />
              </el-form-item>

              <el-form-item prop="password">
                <el-input
                  v-model="loginForm.password"
                  type="password"
                  placeholder="请输入密码"
                  show-password
                  :prefix-icon="Lock"
                  @keyup.enter="handleLogin"
                />
              </el-form-item>

              <div class="agree-row">
                <el-checkbox v-model="loginForm.agree">
                  我已阅读并同意
                </el-checkbox>
                <el-link
                  type="primary"
                  :underline="false"
                  @click="showProtocol('服务')"
                  >《用户服务协议》</el-link
                >
                <span class="agree-and">与</span>
                <el-link
                  type="primary"
                  :underline="false"
                  @click="showProtocol('隐私')"
                  >《隐私政策》</el-link
                >
              </div>

              <el-button
                class="submit-btn"
                :loading="loading"
                :disabled="!loginForm.agree"
                @click="handleLogin"
              >
                登 录
              </el-button>
            </el-form>
          </el-tab-pane>

          <!-- 注册 -->
          <el-tab-pane label="注 册" name="register">
            <el-form
              :model="registerForm"
              :rules="registerRules"
              ref="registerFormRef"
              size="large"
              hide-required-asterisk
            >
              <el-form-item prop="username">
                <el-input
                  v-model="registerForm.username"
                  placeholder="请输入账号"
                  :prefix-icon="User"
                />
              </el-form-item>

              <el-form-item prop="password">
                <el-input
                  v-model="registerForm.password"
                  type="password"
                  placeholder="请输入密码"
                  show-password
                  :prefix-icon="Lock"
                />
              </el-form-item>

              <el-form-item prop="confirmPassword">
                <el-input
                  v-model="registerForm.confirmPassword"
                  type="password"
                  placeholder="请再次输入密码"
                  show-password
                  :prefix-icon="Lock"
                  @keyup.enter="handleRegister"
                />
              </el-form-item>

              <div class="agree-row">
                <el-checkbox v-model="registerForm.agree">
                  我已阅读并同意
                </el-checkbox>
                <el-link
                  type="primary"
                  :underline="false"
                  @click="showProtocol('服务')"
                  >《用户服务协议》</el-link
                >
                <span class="agree-and">与</span>
                <el-link
                  type="primary"
                  :underline="false"
                  @click="showProtocol('隐私')"
                  >《隐私政策》</el-link
                >
              </div>

              <el-button
                class="submit-btn"
                :loading="loading"
                :disabled="!registerForm.agree"
                @click="handleRegister"
              >
                注 册
              </el-button>
            </el-form>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { User, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { login, register } from '../api/auth'

const router = useRouter()
const activeTab = ref('login')
const loading = ref(false)

const loginFormRef = ref(null)
const registerFormRef = ref(null)

const loginForm = reactive({
  username: '',
  password: '',
  agree: false
})

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  agree: false
})

const loginRules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const validateConfirm = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const registerRules = {
  username: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { min: 2, max: 20, message: '账号长度 2-20 位', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirm, trigger: 'blur' }
  ]
}

const showProtocol = (type) => {
  ElMessage.info(`《${type === '服务' ? '用户服务协议' : '隐私政策'}》页面待开发`)
}

const handleLogin = async () => {
  if (!loginForm.agree) {
    ElMessage.warning('请先勾选同意用户服务协议与隐私政策')
    return
  }
  const valid = await loginFormRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res = await login({
      username: loginForm.username,
      password: loginForm.password
    })
    ElMessage.success('登录成功')
    localStorage.setItem('token', res.data.token)
    localStorage.setItem('user', JSON.stringify(res.data.user))
    router.push('/')
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleRegister = async () => {
  if (!registerForm.agree) {
    ElMessage.warning('请先勾选同意用户服务协议与隐私政策')
    return
  }
  const valid = await registerFormRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await register({
      username: registerForm.username,
      password: registerForm.password
    })
    ElMessage.success('注册成功，请登录')

    loginForm.username = registerForm.username
    loginForm.password = ''
    registerForm.username = ''
    registerForm.password = ''
    registerForm.confirmPassword = ''
    registerForm.agree = false
    activeTab.value = 'login'
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  height: 100vh;
  width: 100vw;
  display: flex;
  overflow: hidden;
  background: #f5f6fa;
}

/* 左侧形象区 */
.login-left {
  flex: 1.4;
  position: relative;
  background-image: url('https://images.unsplash.com/photo-1534438327276-14e5300c3a48?w=1400&q=80');
  background-size: cover;
  background-position: center;
  color: #fff;
  overflow: hidden;
}

.left-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    135deg,
    rgba(20, 20, 28, 0.65) 0%,
    rgba(20, 20, 28, 0.35) 60%,
    rgba(20, 20, 28, 0.55) 100%
  );
}

.left-content {
  position: relative;
  z-index: 1;
  height: 100%;
  padding: 60px 70px;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
}

.brand {
  position: absolute;
  top: 60px;
  left: 70px;
  display: flex;
  align-items: center;
  gap: 14px;
}

.brand-logo {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  background: linear-gradient(135deg, #ff8c42 0%, #ff6b1a 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 6px 18px rgba(255, 122, 47, 0.45);
}

.logo-icon {
  font-size: 28px;
  line-height: 1;
}

.brand-name {
  font-size: 22px;
  font-weight: 800;
  letter-spacing: 2px;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.4);
}

.left-title {
  font-size: 44px;
  font-weight: 700;
  margin: 0 0 16px 0;
  letter-spacing: 4px;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.4);
}

.left-subtitle {
  font-size: 18px;
  margin: 0 0 60px 0;
  opacity: 0.9;
  letter-spacing: 1px;
}

/* 右侧表单区 */
.login-right {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background: #fafbfc;
}

.login-card {
  width: 100%;
  max-width: 380px;
  padding: 36px 36px 32px;
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.08);
}

.auth-tabs :deep(.el-tabs__header) {
  margin-bottom: 28px;
}

.auth-tabs :deep(.el-tabs__nav-wrap::after) {
  height: 1px;
  background: #eee;
}

.auth-tabs :deep(.el-tabs__item) {
  font-size: 18px;
  font-weight: 600;
  color: #888;
  padding: 0 24px;
  height: 48px;
  line-height: 48px;
}

.auth-tabs :deep(.el-tabs__item.is-active) {
  color: #ff7a2f;
}

.auth-tabs :deep(.el-tabs__active-bar) {
  background-color: #ff7a2f;
  height: 3px;
  border-radius: 3px;
}

.auth-tabs :deep(.el-input__wrapper) {
  padding: 4px 14px;
  border-radius: 10px;
  background: #f5f6f8;
  box-shadow: none !important;
  border: 1px solid transparent;
  transition: all 0.2s;
}

.auth-tabs :deep(.el-input__wrapper.is-focus),
.auth-tabs :deep(.el-input__wrapper:hover) {
  background: #fff;
  border-color: #ff7a2f;
}

.auth-tabs :deep(.el-input__inner) {
  height: 44px;
}

.auth-tabs :deep(.el-input__prefix) {
  color: #999;
  font-size: 18px;
}

.agree-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  font-size: 13px;
  color: #666;
  margin: -8px 0 18px;
  line-height: 1.6;
}

.agree-row :deep(.el-checkbox__label) {
  font-size: 13px;
  color: #666;
  padding-left: 6px;
}

.agree-row :deep(.el-checkbox__inner) {
  border-radius: 3px;
}

.agree-row :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background-color: #ff7a2f;
  border-color: #ff7a2f;
}

.agree-row :deep(.el-checkbox__input.is-checked + .el-checkbox__label) {
  color: #666;
}

.agree-row .el-link {
  font-size: 13px;
  color: #ff7a2f;
  vertical-align: baseline;
}

.agree-and {
  margin: 0 2px;
}

.submit-btn {
  width: 100%;
  height: 46px;
  border: none;
  border-radius: 10px;
  background: linear-gradient(135deg, #ff8c42 0%, #ff6b1a 100%);
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 6px;
  cursor: pointer;
  transition: all 0.25s;
  box-shadow: 0 6px 16px rgba(255, 107, 26, 0.3);
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 8px 20px rgba(255, 107, 26, 0.4);
}

.submit-btn:disabled {
  opacity: 0.55;
  cursor: not-allowed;
  box-shadow: none;
}

/* 响应式：小屏隐藏左侧形象区 */
@media (max-width: 900px) {
  .login-left {
    display: none;
  }
  .login-right {
    flex: 1;
    background: linear-gradient(135deg, #2d2d3a 0%, #1a1a24 100%);
  }
}
</style>
