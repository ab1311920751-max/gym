<template>
  <div>
    <div class="page-header">
      <h2 class="page-title">
        <el-icon><Bell /></el-icon>
        <span>公告管理</span>
      </h2>
      <p class="page-subtitle">发布、编辑健身房系统公告</p>
    </div>

    <el-card shadow="never" class="content-card">
      <div class="toolbar">
        <el-input
          v-model="query.title"
          placeholder="按标题搜索"
          clearable
          :prefix-icon="Search"
          class="search-input"
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        />
        <el-select
          v-model="query.status"
          placeholder="全部状态"
          clearable
          style="width: 130px"
          @change="handleSearch"
        >
          <el-option label="草稿" :value="0" />
          <el-option label="已发布" :value="1" />
        </el-select>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <div class="toolbar-spacer" />
        <el-button type="primary" :icon="Plus" @click="handleAdd">新增公告</el-button>
      </div>

      <el-table v-loading="loading" :data="tableData" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="content" label="内容摘要" min-width="260" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-popconfirm title="确定删除该公告？" @confirm="handleDelete(row.id)">
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
          :page-sizes="[8, 20, 50]"
          background
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="load"
          @size-change="load"
        />
      </div>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="form.id ? '编辑公告' : '新增公告'"
      width="580px"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="公告标题" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="6"
            placeholder="公告内容"
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="0">草稿</el-radio>
            <el-radio :value="1">立即发布</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Bell, Search, Plus } from '@element-plus/icons-vue'
import { pageNotices, addNotice, updateNotice, deleteNotice } from '../api/notice'

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const tableData = ref([])
const total = ref(0)
const formRef = ref(null)

const query = reactive({ pageNum: 1, pageSize: 8, title: '', status: null })

const emptyForm = () => ({ id: null, title: '', content: '', status: 1 })
const form = ref(emptyForm())

const rules = {
  title: [{ required: true, message: '请填写公告标题', trigger: 'blur' }],
  content: [{ required: true, message: '请填写公告内容', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const formatTime = (time) => {
  if (!time) return ''
  return time.replace('T', ' ').slice(0, 16)
}

const load = async () => {
  loading.value = true
  try {
    const res = await pageNotices({
      pageNum: query.pageNum,
      pageSize: query.pageSize,
      title: query.title || undefined,
      status: query.status != null ? query.status : undefined
    })
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  query.pageNum = 1
  load()
}

const handleAdd = () => {
  form.value = emptyForm()
  dialogVisible.value = true
  setTimeout(() => formRef.value?.clearValidate(), 0)
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
    if (form.value.id) {
      await updateNotice(form.value)
    } else {
      await addNotice(form.value)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}

const handleDelete = async (id) => {
  try {
    await deleteNotice(id)
    ElMessage.success('删除成功')
    load()
  } catch (e) {
    console.error(e)
  }
}

onMounted(() => load())
</script>

<style scoped>
.page-header {
  margin-bottom: 24px;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 22px;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0 0 6px;
}

.page-subtitle {
  color: #909399;
  font-size: 14px;
  margin: 0;
}

.content-card {
  border-radius: 12px;
}

.toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.search-input {
  width: 220px;
}

.toolbar-spacer {
  flex: 1;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
