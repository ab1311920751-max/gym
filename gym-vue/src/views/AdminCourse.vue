<template>
  <div class="admin-course-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">
          <el-icon><Tickets /></el-icon>
          <span>课程管理</span>
        </h2>
        <p class="page-subtitle">创建、编辑、上下架课程</p>
      </div>
    </div>

    <el-card shadow="never" class="content-card">
      <div class="toolbar">
        <el-input
          v-model="query.name"
          placeholder="按课程名搜索"
          clearable
          :prefix-icon="Search"
          class="search-input"
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        />
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <div class="toolbar-spacer"></div>
        <el-button type="primary" :icon="Plus" @click="handleAdd">新增课程</el-button>
      </div>

      <el-table
        v-loading="loading"
        :data="tableData"
        stripe
        empty-text="暂无课程"
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="课程名称" min-width="140" />
        <el-table-column prop="coach" label="教练" width="120" />
        <el-table-column label="开课时间" width="170">
          <template #default="{ row }">
            {{ formatTime(row.startTime) }}
          </template>
        </el-table-column>
        <el-table-column label="价格" width="110">
          <template #default="{ row }">
            <span class="price">￥{{ row.price }}</span>
          </template>
        </el-table-column>
        <el-table-column label="库存 / 容量" width="120">
          <template #default="{ row }">
            <span :class="{ low: row.stock < 3 }">{{ row.stock }}</span>
            <span class="capacity">/ {{ row.capacity || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-popconfirm
              title="确定删除该课程？该操作不可恢复。"
              width="240"
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

    <!-- 编辑/新增弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="form.id ? '编辑课程' : '新增课程'"
      width="560px"
    >
      <el-form
        :model="form"
        :rules="rules"
        ref="formRef"
        label-width="100px"
        label-position="right"
      >
        <el-form-item label="课程名称" prop="name">
          <el-input v-model="form.name" placeholder="如：HIIT 燃脂特训" />
        </el-form-item>
        <el-form-item label="教练姓名" prop="coach">
          <el-input v-model="form.coach" placeholder="如：张教练" />
        </el-form-item>
        <el-form-item label="课程简介" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="3"
            placeholder="一句话描述课程亮点"
          />
        </el-form-item>
        <el-form-item label="开课时间" prop="startTime">
          <el-date-picker
            v-model="form.startTime"
            type="datetime"
            placeholder="选择开课时间"
            value-format="YYYY-MM-DDTHH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="价格 (元)" prop="price">
          <el-input-number
            v-model="form.price"
            :precision="2"
            :step="10"
            :min="0"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="容量上限" prop="capacity">
          <el-input-number v-model="form.capacity" :min="1" :max="9999" style="width: 100%" />
        </el-form-item>
        <el-form-item label="可约库存" prop="stock">
          <el-input-number v-model="form.stock" :min="0" :max="9999" style="width: 100%" />
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
import { Search, Plus, Tickets } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import {
  pageCourses,
  addCourse,
  updateCourse,
  deleteCourse
} from '../api/course'

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const tableData = ref([])
const total = ref(0)
const formRef = ref(null)

const query = reactive({
  pageNum: 1,
  pageSize: 10,
  name: ''
})

const emptyForm = () => ({
  id: null,
  name: '',
  coach: '',
  content: '',
  startTime: '',
  price: 0,
  capacity: 20,
  stock: 20
})

const form = ref(emptyForm())

const rules = {
  name: [{ required: true, message: '请输入课程名称', trigger: 'blur' }],
  coach: [{ required: true, message: '请输入教练姓名', trigger: 'blur' }],
  startTime: [{ required: true, message: '请选择开课时间', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  capacity: [{ required: true, message: '请输入容量', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'blur' }]
}

const load = async () => {
  loading.value = true
  try {
    const res = await pageCourses({
      pageNum: query.pageNum,
      pageSize: query.pageSize,
      name: query.name || undefined
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

const handleAdd = () => {
  form.value = emptyForm()
  dialogVisible.value = true
  // 等弹窗渲染再清校验
  setTimeout(() => formRef.value?.clearValidate(), 0)
}

const handleEdit = (row) => {
  form.value = JSON.parse(JSON.stringify(row))
  dialogVisible.value = true
  setTimeout(() => formRef.value?.clearValidate(), 0)
}

const handleDelete = async (id) => {
  try {
    await deleteCourse(id)
    ElMessage.success('删除成功')
    load()
  } catch (e) {
    console.error(e)
  }
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
      await updateCourse(form.value)
    } else {
      await addCourse(form.value)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    load()
  } catch (e) {
    console.error(e)
  } finally {
    saving.value = false
  }
}

const formatTime = (val) => (val ? dayjs(val).format('YYYY-MM-DD HH:mm') : '—')

onMounted(() => load())
</script>

<style scoped>
.admin-course-page {
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

.toolbar-spacer {
  flex: 1;
}

.price {
  color: #ff7a2f;
  font-weight: 600;
}

.low {
  color: #f56c6c;
  font-weight: 600;
}

.capacity {
  color: #909399;
  font-size: 13px;
  margin-left: 2px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  padding-top: 16px;
}
</style>
