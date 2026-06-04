<template>
  <div class="profile-page">
    <!-- 顶部用户信息卡 -->
    <el-card shadow="never" class="user-hero-card">
      <div class="user-hero-inner">
        <div class="big-avatar">
          {{ (user.username || 'U').charAt(0).toUpperCase() }}
        </div>
        <div class="user-hero-info">
          <div class="user-hero-name">{{ user.username || '—' }}</div>
          <el-tag :type="VIP_TAG_TYPE[user.vipType || 0]" effect="light" size="small" class="vip-tag">
            {{ VIP_LABEL[user.vipType || 0] }}
          </el-tag>
          <div v-if="user.vipType > 0" class="vip-expire-line" :class="{ warn: expiring }">
            <el-icon><AlarmClock /></el-icon>
            <span>VIP 到期：{{ formatDate(user.vipExpireTime) }}</span>
            <span v-if="expiring" class="expire-warn">（即将过期）</span>
          </div>
        </div>
      </div>
    </el-card>

    <el-row :gutter="20" class="bottom-row">
      <!-- 个人资料 -->
      <el-col :xs="24" :md="14">
        <el-card shadow="never" class="section-card">
          <template #header>
            <div class="card-header-row">
              <span class="card-header-title">个人资料</span>
              <el-button
                v-if="!editingProfile"
                :icon="Edit"
                size="small"
                type="primary"
                plain
                @click="startEditProfile"
              >
                编辑
              </el-button>
            </div>
          </template>

          <el-form
            :model="profileForm"
            :rules="profileRules"
            ref="profileFormRef"
            label-width="80px"
            :disabled="!editingProfile"
          >
            <el-form-item label="用户名" prop="username">
              <el-input
                v-model="profileForm.username"
                maxlength="20"
                show-word-limit
                placeholder="请输入用户名"
              />
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
              <el-input
                v-model="profileForm.phone"
                maxlength="11"
                placeholder="未填写"
              />
            </el-form-item>
            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="profileForm.gender">
                <el-radio :value="null">保密</el-radio>
                <el-radio :value="1">男</el-radio>
                <el-radio :value="2">女</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input
                v-model="profileForm.email"
                placeholder="未填写"
              />
            </el-form-item>
            <el-form-item v-if="editingProfile">
              <el-button type="primary" @click="handleSaveProfile">保存</el-button>
              <el-button @click="cancelEditProfile">取消</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <!-- 修改密码 -->
      <el-col :xs="24" :md="10">
        <el-card shadow="never" class="section-card">
          <template #header>
            <div class="card-header-row">
              <span class="card-header-title">修改密码</span>
            </div>
          </template>

          <el-form
            :model="passwordForm"
            :rules="passwordRules"
            ref="passwordFormRef"
            label-width="100px"
          >
            <el-form-item label="原密码" prop="oldPassword">
              <el-input
                v-model="passwordForm.oldPassword"
                type="password"
                show-password
                placeholder="请输入原密码"
              />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input
                v-model="passwordForm.newPassword"
                type="password"
                show-password
                placeholder="至少 6 位"
              />
            </el-form-item>
            <el-form-item label="确认新密码" prop="confirmPassword">
              <el-input
                v-model="passwordForm.confirmPassword"
                type="password"
                show-password
                placeholder="再次输入新密码"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleChangePassword">确认修改</el-button>
              <el-button @click="resetPasswordForm">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, reactive, onMounted } from 'vue'
import { AlarmClock, Edit } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import { getUserById, updateProfile, changePassword } from '../api/user'
import { VIP_LABEL, VIP_TAG_TYPE } from '../constants/vip'

const user = ref({})
const editingProfile = ref(false)
const profileFormRef = ref(null)
const passwordFormRef = ref(null)

const profileForm = reactive({ username: '', phone: '', gender: null, email: '' })
const profileRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 1, max: 20, message: '用户名长度在 1 到 20 个字符', trigger: 'blur' },
    { pattern: /^\S+$/, message: '不能包含空格', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入有效的手机号', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' }
  ]
}

const passwordForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const validateConfirmPassword = (rule, value, cb) => {
  if (value !== passwordForm.newPassword) {
    cb(new Error('两次输入的密码不一致'))
  } else {
    cb()
  }
}
const passwordRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于 6 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const expiring = computed(() => {
  if (!user.value.vipExpireTime) return false
  const expireTime = dayjs(user.value.vipExpireTime)
  const now = dayjs()
  return expireTime.diff(now, 'day') <= 7 && expireTime.isAfter(now)
})

const formatDate = (dateStr) => {
  if (!dateStr) return '永久有效'
  return dayjs(dateStr).format('YYYY年MM月DD日 HH:mm')
}

const loadUser = async () => {
  const localUser = JSON.parse(localStorage.getItem('user') || '{}')
  if (!localUser.id) return
  try {
    const res = await getUserById(localUser.id)
    if (res.code === '200') {
      user.value = res.data
      localStorage.setItem('user', JSON.stringify(res.data))
    }
  } catch (e) {
    console.error(e)
  }
}

const startEditProfile = () => {
  profileForm.username = user.value.username || ''
  profileForm.phone = user.value.phone || ''
  profileForm.gender = user.value.gender != null ? user.value.gender : null
  profileForm.email = user.value.email || ''
  editingProfile.value = true
}

const cancelEditProfile = () => {
  editingProfile.value = false
  profileFormRef.value?.clearValidate()
}

const handleSaveProfile = async () => {
  try {
    await profileFormRef.value.validate()
  } catch { return }
  try {
    await updateProfile({
      username: profileForm.username,
      phone: profileForm.phone || undefined,
      gender: profileForm.gender,
      email: profileForm.email || undefined
    })
    ElMessage.success('个人资料修改成功')
    editingProfile.value = false
    await loadUser()
    window.dispatchEvent(new Event('refresh-user'))
  } catch (e) {
    console.error(e)
  }
}

const resetPasswordForm = () => {
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  passwordFormRef.value?.clearValidate()
}

const handleChangePassword = async () => {
  try {
    await passwordFormRef.value.validate()
  } catch { return }
  try {
    await changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    ElMessage.success('密码修改成功，请牢记新密码')
    resetPasswordForm()
  } catch (e) {
    console.error(e)
  }
}

onMounted(() => {
  const localUser = JSON.parse(localStorage.getItem('user') || '{}')
  user.value = localUser
  loadUser()
})
</script>

<style scoped>
.profile-page {
  padding: 4px;
}

/* 顶部英雄卡 */
.user-hero-card {
  border: none;
  border-radius: 10px;
  margin-bottom: 20px;
}

.user-hero-inner {
  display: flex;
  align-items: center;
  gap: 20px;
}

.big-avatar {
  width: 72px;
  height: 72px;
  border-radius: 16px;
  background: linear-gradient(135deg, #ff8c42 0%, #ff6b1a 100%);
  color: #fff;
  font-size: 30px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 6px 16px rgba(255, 107, 26, 0.3);
  flex-shrink: 0;
}

.user-hero-name {
  font-size: 22px;
  font-weight: 700;
  color: #1f2d3d;
  margin-bottom: 6px;
}

.vip-tag {
  margin-bottom: 8px;
}

.vip-expire-line {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #606266;
  margin-top: 6px;
}

.vip-expire-line.warn {
  color: #f56c6c;
}

.expire-warn {
  font-weight: 600;
}

/* 下方区域 */
.bottom-row {
  align-items: flex-start;
}

.section-card {
  border: none;
  border-radius: 10px;
  margin-bottom: 16px;
}

.card-header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-header-title {
  font-size: 16px;
  font-weight: 700;
  color: #1f2d3d;
}
</style>
