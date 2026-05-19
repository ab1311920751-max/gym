<template>
  <div class="ai-chat-container">
    <!-- 左侧会话列表 -->
    <div class="session-sidebar">
      <div class="sidebar-header">
        <el-button type="primary" :icon="Plus" @click="newChat" class="new-chat-btn">
          新建对话
        </el-button>
      </div>
      <div class="session-list" v-loading="sessionLoading">
        <div v-if="sessions.length === 0 && !sessionLoading" class="empty-sessions">
          <p>暂无对话记录</p>
          <p class="sub">点击上方按钮开始新对话</p>
        </div>
        <div
          v-for="s in sessions"
          :key="s.id"
          class="session-item"
          :class="{ active: selectedSession && selectedSession.id === s.id }"
          @click="selectSession(s)"
        >
          <div class="session-content">
            <div class="session-title">{{ s.title }}</div>
            <div class="session-preview">{{ s.lastMessage }}</div>
          </div>
          <el-popconfirm
            title="确定删除该对话?"
            confirm-button-text="删除"
            cancel-button-text="取消"
            @confirm="handleDelete(s.id, $event)"
          >
            <template #reference>
              <el-button class="delete-btn" text size="small" :icon="Delete" @click.stop />
            </template>
          </el-popconfirm>
        </div>
      </div>
    </div>

    <!-- 右侧聊天区域 -->
    <div class="chat-main">
      <!-- 空状态 -->
      <div v-if="!selectedSession" class="welcome-area">
        <div class="welcome-icon">🏋️</div>
        <h2>你好，我是你的 AI 健身助手</h2>
        <p class="welcome-desc">我可以帮你查询课程、推荐训练、解答疑问</p>
        <div class="quick-questions">
          <p class="quick-title">试试问我：</p>
          <div class="quick-tags">
            <el-tag
              v-for="q in quickQuestions"
              :key="q"
              class="quick-tag"
              @click="sendQuick(q)"
            >
              {{ q }}
            </el-tag>
          </div>
        </div>
      </div>

      <!-- 聊天界面 -->
      <template v-else>
        <div class="chat-messages" ref="messageContainer">
          <div v-if="messages.length === 0 && !msgLoading" class="chat-empty">
            开始对话吧
          </div>
          <div v-loading="msgLoading" class="msg-loading-area">
            <div
              v-for="m in messages"
              :key="m.id"
              class="message-row"
              :class="m.role === 'user' ? 'msg-user' : 'msg-assistant'"
            >
              <div v-if="m.role === 'assistant'" class="msg-avatar">
                <div class="ai-avatar">AI</div>
              </div>
              <div class="msg-bubble" :class="m.role">
                <div class="msg-text">{{ m.content }}</div>
              </div>
              <div v-if="m.role === 'user'" class="msg-avatar">
                <div class="user-avatar-sm">{{ user.username ? user.username.charAt(0).toUpperCase() : 'U' }}</div>
              </div>
            </div>
            <div v-if="sending" class="message-row msg-assistant">
              <div class="msg-avatar">
                <div class="ai-avatar">AI</div>
              </div>
              <div class="msg-bubble assistant typing-bubble">
                <span class="typing-dots"><span>.</span><span>.</span><span>.</span></span>
              </div>
            </div>
          </div>
        </div>

        <div class="chat-input-area">
          <el-input
            v-model="inputText"
            type="textarea"
            :rows="1"
            placeholder="输入你的问题，按 Enter 发送，Shift+Enter 换行"
            @keydown.enter.exact.prevent="send(inputText)"
            :disabled="sending"
            resize="none"
            class="msg-input"
          />
          <el-button
            type="primary"
            :icon="Promotion"
            @click="send(inputText)"
            :disabled="!inputText.trim() || sending"
            :loading="sending"
            class="send-btn"
          >
            发送
          </el-button>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Delete, Promotion } from '@element-plus/icons-vue'
import { sendMessage, getSessions, getMessages, deleteSession } from '../api/ai'

const user = JSON.parse(localStorage.getItem('user') || '{}')

const sessions = ref([])
const sessionLoading = ref(false)
const selectedSession = ref(null)
const messages = ref([])
const msgLoading = ref(false)
const sending = ref(false)
const inputText = ref('')
const messageContainer = ref(null)

const quickQuestions = [
  '最近有什么课推荐？',
  '明天有什么课？',
  '还剩多少名额？',
  '我的余额是多少？'
]

onMounted(async () => {
  await loadSessions()
})

async function loadSessions() {
  sessionLoading.value = true
  try {
    const res = await getSessions()
    sessions.value = res.data || []
  } catch (e) {
    ElMessage.error('加载会话列表失败')
  } finally {
    sessionLoading.value = false
  }
}

function newChat() {
  selectedSession.value = null
  messages.value = []
}

async function selectSession(session) {
  selectedSession.value = session
  msgLoading.value = true
  try {
    const res = await getMessages(session.id)
    messages.value = res.data || []
    await nextTick()
    scrollToBottom()
  } catch (e) {
    ElMessage.error('加载消息失败')
  } finally {
    msgLoading.value = false
  }
}

async function send(text) {
  const msg = text.trim()
  if (!msg) return
  inputText.value = ''
  sending.value = true

  const tempId = Date.now()
  messages.value.push({
    id: tempId,
    sessionId: selectedSession.value ? selectedSession.value.id : null,
    role: 'user',
    content: msg,
    createTime: new Date().toISOString()
  })
  await nextTick()
  scrollToBottom()

  try {
    const res = await sendMessage({
      sessionId: selectedSession.value ? selectedSession.value.id : null,
      message: msg
    })
    // remove temp user message if it was a new session
    const reply = res.data
    if (!selectedSession.value) {
      // new session created - reload sessions and set as selected
      const sessionMsg = messages.value.find(m => m.id === tempId)
      if (sessionMsg) {
        sessionMsg.sessionId = reply.sessionId
      }
      await loadSessions()
      const newSession = sessions.value.find(s => s.id === reply.sessionId)
      if (newSession) {
        selectedSession.value = newSession
      }
    }
    messages.value.push(reply)
    await nextTick()
    scrollToBottom()
  } catch (e) {
    ElMessage.error('发送失败，请重试')
  } finally {
    sending.value = false
  }
}

function sendQuick(q) {
  inputText.value = q
  send(q)
}

async function handleDelete(sessionId, event) {
  if (event) event.stopPropagation()
  try {
    await deleteSession(sessionId)
    sessions.value = sessions.value.filter(s => s.id !== sessionId)
    if (selectedSession.value && selectedSession.value.id === sessionId) {
      selectedSession.value = null
      messages.value = []
    }
    ElMessage.success('已删除')
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

function scrollToBottom() {
  if (messageContainer.value) {
    messageContainer.value.scrollTop = messageContainer.value.scrollHeight
  }
}
</script>

<style scoped>
.ai-chat-container {
  display: flex;
  height: 100%;
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

/* ======== 左侧会话列表 ======== */
.session-sidebar {
  width: 280px;
  border-right: 1px solid #ebeef5;
  display: flex;
  flex-direction: column;
  background: #fafafa;
  flex-shrink: 0;
}

.sidebar-header {
  padding: 16px;
  border-bottom: 1px solid #ebeef5;
}

.new-chat-btn {
  width: 100%;
}

.session-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.empty-sessions {
  text-align: center;
  color: #999;
  padding: 40px 16px;
}
.empty-sessions p { margin: 0; }
.empty-sessions .sub { font-size: 12px; margin-top: 6px; }

.session-item {
  display: flex;
  align-items: center;
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  margin-bottom: 4px;
  transition: background 0.2s;
}
.session-item:hover { background: #f0f0f0; }
.session-item.active { background: #fff5ee; border-left: 3px solid #ff7a2f; }

.session-content {
  flex: 1;
  min-width: 0;
}
.session-title {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.session-preview {
  font-size: 12px;
  color: #999;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-top: 4px;
}
.delete-btn {
  opacity: 0;
  transition: opacity 0.2s;
  flex-shrink: 0;
}
.session-item:hover .delete-btn { opacity: 1; }

/* ======== 右侧聊天区域 ======== */
.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

/* 空状态 */
.welcome-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
}
.welcome-icon { font-size: 64px; margin-bottom: 16px; }
.welcome-area h2 { font-size: 22px; color: #303133; margin: 0 0 8px; }
.welcome-desc { color: #909399; margin: 0 0 32px; }
.quick-questions { text-align: center; }
.quick-title { font-size: 13px; color: #909399; margin-bottom: 12px; }
.quick-tags { display: flex; gap: 10px; flex-wrap: wrap; justify-content: center; }
.quick-tag {
  cursor: pointer;
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 13px;
  transition: all 0.2s;
}
.quick-tag:hover {
  background: #ff7a2f;
  color: #fff;
  border-color: #ff7a2f;
}

/* 消息列表 */
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px 24px;
}
.chat-empty {
  text-align: center;
  color: #ccc;
  padding-top: 60px;
  font-size: 14px;
}

.msg-loading-area {
  min-height: 100%;
}

.message-row {
  display: flex;
  align-items: flex-start;
  margin-bottom: 20px;
}

.msg-user {
  justify-content: flex-end;
}
.msg-assistant {
  justify-content: flex-start;
}

.msg-avatar {
  flex-shrink: 0;
  width: 36px;
  height: 36px;
  margin: 0 10px;
}
.ai-avatar {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background: linear-gradient(135deg, #ff8c42 0%, #ff6b1a 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: bold;
}
.user-avatar-sm {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background: #c0c4cc;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: bold;
}

.msg-bubble {
  max-width: 65%;
  padding: 10px 16px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.7;
  word-break: break-word;
}
.msg-bubble.user {
  background: linear-gradient(135deg, #ff8c42 0%, #ff6b1a 100%);
  color: #fff;
  border-bottom-right-radius: 4px;
}
.msg-bubble.assistant {
  background: #f5f7fa;
  color: #303133;
  border-bottom-left-radius: 4px;
}

.msg-text {
  white-space: pre-wrap;
}

/* 打字动画 */
.typing-bubble {
  padding: 14px 20px;
}
.typing-dots span {
  display: inline-block;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #c0c4cc;
  margin: 0 2px;
  animation: typing 1.4s infinite both;
}
.typing-dots span:nth-child(2) { animation-delay: 0.2s; }
.typing-dots span:nth-child(3) { animation-delay: 0.4s; }

@keyframes typing {
  0%, 60%, 100% { transform: translateY(0); opacity: 0.4; }
  30% { transform: translateY(-8px); opacity: 1; }
}

/* 输入区 */
.chat-input-area {
  display: flex;
  align-items: flex-end;
  gap: 10px;
  padding: 16px 24px;
  border-top: 1px solid #ebeef5;
  background: #fff;
}
.msg-input {
  flex: 1;
}
.send-btn {
  flex-shrink: 0;
}
</style>
