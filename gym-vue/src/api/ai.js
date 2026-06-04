import request from '../utils/request'

// AI 聊天接口，对应后端 /ai 路由
// sendMessageStream 使用 fetch + ReadableStream 实现 SSE 流式接收（打字机效果）

/** 同步发送消息，等 AI 完整回复后一次性返回。data: { sessionId, message } */
export const sendMessage = (data) => request.post('/ai/chat', data)

/** 获取当前用户的会话列表，按更新时间倒序 */
export const getSessions = () => request.get('/ai/sessions')

/** 获取指定会话的所有消息，按时间正序 */
export const getMessages = (sessionId) => request.get(`/ai/sessions/${sessionId}/messages`)

/** 删除指定会话及其所有消息 */
export const deleteSession = (sessionId) => request.delete(`/ai/sessions/${sessionId}`)

/**
 * 流式发送消息（fetch + ReadableStream），实现打字机效果。
 * 首帧接收 session 元数据（含 sessionId），中间帧为文字 chunk，末帧为 [DONE]。
 * @param {Object} data - { sessionId, message }
 * @param {Function} onSession - (sessionId: number) => void，收到首帧 session 元数据时触发
 * @param {Function} onChunk  - (chunk: string) => void，每个文字 chunk 触发
 * @param {Function} onDone   - () => void，流结束时触发
 * @param {Function} onError  - (err: Error) => void，异常时触发
 */
export const sendMessageStream = (data, onSession, onChunk, onDone, onError) => {
  const token = localStorage.getItem('token') || ''
  fetch('http://localhost:8080/ai/chat/stream', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': token
    },
    body: JSON.stringify(data)
  })
    .then(res => {
      if (!res.ok) {
        onError(new Error('请求失败: ' + res.status))
        return
      }
      const reader = res.body.getReader()
      const decoder = new TextDecoder()

      const read = () => {
        reader.read().then(({ done, value }) => {
          if (done) {
            onDone()
            return
          }
          const text = decoder.decode(value, { stream: true })
          text.split('\n').forEach(line => {
            const trimmed = line.trim()
            if (!trimmed.startsWith('data:')) return
            const chunk = trimmed.slice(5).trim()
            if (!chunk) return
            if (chunk === '[DONE]') {
              onDone()
              return
            }
            if (chunk === '[ERROR]') {
              onError(new Error('AI 服务异常，请稍后重试'))
              return
            }
            if (chunk.startsWith('{')) {
              try {
                const meta = JSON.parse(chunk)
                if (meta.type === 'session') {
                  onSession(meta.sessionId)
                }
              } catch {
                onChunk(chunk)
              }
            } else {
              onChunk(chunk)
            }
          })
          read()
        }).catch(onError)
      }
      read()
    })
    .catch(onError)
}
