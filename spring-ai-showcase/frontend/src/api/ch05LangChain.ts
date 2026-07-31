import { getChannelStatus, postChannelRequest } from './client'

export type LangChainChatResponse = {
  channel: string
  module: string
  sessionId: string
  message: string
  reply: string
}

export function fetchCh05Status() {
  return getChannelStatus('/ch05/status')
}

export function sendCh05Chat(sessionId: string, message: string) {
  return postChannelRequest<LangChainChatResponse>('/ch05/chat', { sessionId, message })
}
