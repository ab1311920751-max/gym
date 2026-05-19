<template>
  <div class="admin-user-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </h2>
        <p class="page-subtitle">查看用户、调整角色、修改余额与会员等级</p>
      </div>
    </div>

    <el-card shadow="never" class="content-card">
      <div class="toolbar">
        <el-input
          v-model="query.username"
          placeholder="按用户名搜索"
          clearable
          :prefix-icon="Search"
          class="search-input"
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        />
        <el-button type="primary" @click="handleSearch">查询</el-button>
      </div>

      <el-table
        v-loading="loading"
        :data="tableData"
        stripe
        empty-text="暂无用户"
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="username" label="用户名" width="140" />

        <el-table-column label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="row.role === ROLE.ADMIN ? 'danger' : 'info'" effect="light">
              {{ ROLE_LABEL[row.role] || row.role }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="余额" width="120">
          <template #default="{ row }">
            <span class="balance">￥{{ Number(row.balance || 0).toFixed(2) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="会员等级" width="120">
          <template #default="{ row }">
            <el-tag :type="VIP_TAG_TYPE[row.vipType || 0]" effect="light">
              {{ VIP_LABEL[row.vipType || 0] }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="VIP 到期" width="180">
          <template #default="{ row }">
            <template v-if="row.vipType > 0">
              <div class="time-cell">
                <el-icon><AlarmClock /></el-icon>
                <span>{{ formatTime(row.vipExpireTime) }}</span>
              </div>
            </template>
            <span v-else class="muted">—</span>
          </template>
        </el-table-column>

        <el-table-column label="注册时间" width="180">
          <template #default="{ row }">
            <span class="muted">{{ formatTime(row.createTime) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" min-width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-popconfirm
              width="280"
              title="将删除该用户的所有数据，不可恢复，确定吗？"
              @confirm="handleDelete(row.id)"
            >
              <template #reference>
                <el-button size="small" type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          background
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="load"
          @size-change="load"
        />
      </div>
    </el-card>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="dialogVisible" title="编辑用户信息" width="460px">
      <el-form
        :model="form"
        :rules="rules"
        ref="formRef"
        label-width="90px"
      >
        <el-form-item label="用户名">
          <el-input v-model="form.username" disabled />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" style="width: 100%">
            <el-option label="普通用户" :value="ROLE.USER" />
            <el-option label="管理员" :value="ROLE.ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="余额" prop="balance">
          <el-input-number
            v-model="form.balance"
            :precision="2"
            :step="100"
            :min="0"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="会员等级" prop="vipType">
          <el-select v-model="form.vipType" style="width: 100%">
            <el-option label="普通会员" :value="VIP_TYPE.NORMAL" />
            <el-option label="月卡 VIP" :value="VIP_TYPE.MONTHLY" />
            <el-option label="年卡 VIP" :value="VIP_TYPE.YEARLY" />
          </el-select>
          <div class="form-tip">
            直接修改等级不会自动更新到期时间，建议让用户在前台支付开通。
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search, User, AlarmClock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import { pageUsers, updateUser, deleteUser } from '../api/user'
import { ROLE, ROLE_LABEL } from '../constants/role'
import { VIP_TYPE, VIP_LABEL, VIP_TAG_TYPE } from '../constants/vip'

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const tableData = ref([])
const total = ref(0)
const formRef = ref(null)
const form = ref({})

const query = reactive({
  pageNum: 1,
  pageSize: 10,
  username: ''
})

const rules = {
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
  balance: [{ required: true, message: '请输入余额', trigger: 'blur' }],
  vipType: [{ required: true, message: '请选择会员等级', trigger: 'change' }]
}

const load = async () => {
  loading.value = true
  try {
    const res = await pageUsers({
      pageNum: query.pageNum,
      pageSize: query.pageSize,
      username: query.username || undefined
    })
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  query.pageNum = 1
  load()
}

const handleEdit = (row) => {
  form.value = JSON.parse(JSON.stringify(row))
  dialogVisible.value = true
  setTimeout(() => formRef.value?.clearValidate(), 0)
}

const save = async () => {
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  saving.value = true
  try {
    await updateUser(form.value)
    ElMessage.success('更新成功')
    dialogVisible.value = false
    load()
  } catch (e) {
    console.error(e)
  } finally {
    saving.value = false
  }
}

const handleDelete = async (id) => {
  try {
    await deleteUser(id)
    ElMessage.success('删除成功')
    load()
  } catch (e) {
    console.error(e)
  }
}

const formatTime = (val) => (val ? dayjs(val).format('YYYY-MM-DD HH:mm') : '—')

onMounted(() => load())
</script>

<style scoped>
.admin-user-page {
  padding: 4px;
}

.page-header {
  margin-bottom: 16px;
}

.page-title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: #1f2d3d;
  display: flex;
  align-items: center;
  gap: 8px;
}

.page-subtitle {
  color: #909399;
  font-size: 13px;
  margin: 6px 0 0;
}

.content-card {
  border: none;
  border-radius: 10px;
}

.toolbar {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.search-input {
  width: 240px;
}

.balance {
  color: #ff7a2f;
  font-weight: 700;
}

.time-cell {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: #606266;
  font-size: 13px;
}

.muted {
  color: #909399;
  font-size: 13px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  padding-top: 16px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
  margin-top: 6px;
}
</style>
