import request from '../utils/request'

export const sendMessage = (data) => request.post('/ai/chat', data)
export const getSessions = () => request.get('/ai/sessions')
export const getMessages = (sessionId) => request.get(`/ai/sessions/${sessionId}/messages`)
export const deleteSession = (sessionId) => request.delete(`/ai/sessions/${sessionId}`)

/**
 * 流式发送消息（fetch + ReadableStream）
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
