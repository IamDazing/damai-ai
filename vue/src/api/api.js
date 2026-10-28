const BASE_URL = import.meta.env.PROD ? window.location.origin + '/ai-api' : 'http://localhost:6089'
const TIMEOUT = 30000 // 30秒超时
const STREAM_IDLE_TIMEOUT = 45000

// 统一的错误处理
class APIError extends Error {
  constructor(message, status) {
    super(message)
    this.status = status
    this.name = 'APIError'
  }
}

// 统一的请求处理函数
async function fetchWithTimeout(url, options = {}) {
  const controller = new AbortController()
  const timeoutId = setTimeout(() => controller.abort(), TIMEOUT)
  
  try {
    const response = await fetch(url, {
      ...options,
      signal: controller.signal
    })
    
    if (!response.ok) {
      throw new APIError(`HTTP error! status: ${response.status}`, response.status)
    }
    
    return response
  } finally {
    clearTimeout(timeoutId)
  }
}

// 构建URL的辅助函数
function buildUrl(path, params = {}) {
  const url = new URL(`${BASE_URL}${path}`)
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null) {
      url.searchParams.append(key, value)
    }
  })
  return url
}

function streamReader(response) {
  if (!response.body) throw new APIError('响应内容为空', response.status)
  const reader = response.body.getReader()
  return {
    async read() {
      let timer
      try {
        return await Promise.race([
          reader.read(),
          new Promise((_, reject) => {
            timer = setTimeout(() => {
              reader.cancel().catch(() => {})
              const error = new Error('回复超时')
              error.name = 'TimeoutError'
              reject(error)
            }, STREAM_IDLE_TIMEOUT)
          })
        ])
      } finally { clearTimeout(timer) }
    }
  }
}

export function chatErrorMessage(error) {
  if (error?.status === 401) return '登录状态已失效，请返回主站登录后再试。'
  if (error?.status === 503) return '暂时无法验证登录状态，请稍后再试。'
  if (error?.name === 'AbortError' || error?.name === 'TimeoutError') return '回复超时了，请稍后再试。'
  return '抱歉，服务暂时不可用或回复中断，请稍后再试。'
}

export async function checkSession() {
  await fetchWithTimeout(buildUrl('/session'))
}

export const chatAPI = {
  // 发送聊天消息
  async simpleChat(data, chatId) {
    try {
      const url = buildUrl('/simple/chat', { chatId })
      const response = await fetchWithTimeout(url, {
        method: 'POST',
        body: data instanceof FormData ? data : new URLSearchParams({ prompt: data })
      })
      return streamReader(response)
    } catch (error) {
      console.error('Simple Chat Error:', error)
      throw error
    }
  },

  // 获取聊天的历史会话id列表
  async chatTypeHistoryList(type = 1) {
    try {
      const url = buildUrl('/chat/type/history/list', { type })
      const response = await fetchWithTimeout(url)
      const chats = await response.json()
      
      return chats.map(chat => ({
        id:chat.chatId,
        title: chat.title === '' || chat.title === null  || chat.title === undefined ? `新的对话` : chat.title
      }))
    } catch (error) {
      console.error('History Chat ID List Error:', error)
      throw error
    }
  },

  // 获取具体对话下的历史消息
  async chatHistoryMessageList(chatId, type = 1) {
    try {
      const url = buildUrl('/chat/history/message/list', { chatId, type })
      const response = await fetchWithTimeout(url)
      const messages = await response.json()
      
      return messages.map(msg => ({
        ...msg,
        timestamp: new Date()
      }))
    } catch (error) {
      console.error('History Chat History List Error:', error)
      throw error
    }
  },

  // 发送助手消息
  async sendAssistantMessage(prompt, chatId) {
    try {
      const url = buildUrl('/program/chat', { prompt, chatId })
      const response = await fetchWithTimeout(url)
      return streamReader(response)
    } catch (error) {
      console.error('Assistant Message Error:', error)
      throw error
    }
  },

  // 发送rag消息
  async sendRagMessage(prompt, chatId) {
    try {
      const url = buildUrl('/program/rag', { prompt, chatId })
      const response = await fetchWithTimeout(url)
      return streamReader(response)
    } catch (error) {
      console.error('RAG Message Error:', error)
      throw error
    }
  },

  // 发送运维分析消息（MCP日志查询）
  async sendAnalysisMessage(prompt, chatId) {
    try {
      const url = buildUrl('/program/chat/mcp', { prompt, chatId })
      const response = await fetchWithTimeout(url)
      return streamReader(response)
    } catch (error) {
      console.error('Analysis Message Error:', error)
      throw error
    }
  },

  // 删除对话
  async deleteChat(chatId, type = 1) {
    try {
      const url = buildUrl('/chat/delete', { chatId, type })
      await fetchWithTimeout(url)
      return true
    } catch (error) {
      console.error('Delete Chat Error:', error)
      throw error
    }
  }
}

// AI可观测性API
export const observabilityAPI = {
  // 获取今日统计
  async getTodayStats() {
    try {
      const url = buildUrl('/ai/enhance/observability/today')
      const response = await fetchWithTimeout(url)
      const result = await response.json()
      return result.data
    } catch (error) {
      console.error('Get Today Stats Error:', error)
      throw error
    }
  },

  // 获取最近的追踪记录
  async getRecentTraces(limit = 50) {
    try {
      const url = buildUrl('/ai/enhance/observability/traces', { limit })
      const response = await fetchWithTimeout(url)
      const result = await response.json()
      return result.data
    } catch (error) {
      console.error('Get Recent Traces Error:', error)
      throw error
    }
  },

  // 按类型统计
  async getStatsByType() {
    try {
      const url = buildUrl('/ai/enhance/observability/stats/type')
      const response = await fetchWithTimeout(url)
      const result = await response.json()
      return result.data
    } catch (error) {
      console.error('Get Stats By Type Error:', error)
      throw error
    }
  },

  // 获取会话统计
  async getConversationStats(conversationId) {
    try {
      const url = buildUrl('/ai/enhance/observability/conversation', { conversationId })
      const response = await fetchWithTimeout(url)
      const result = await response.json()
      return result.data
    } catch (error) {
      console.error('Get Conversation Stats Error:', error)
      throw error
    }
  }
}
