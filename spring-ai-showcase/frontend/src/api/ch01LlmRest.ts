import { postChannelRequest } from './client'

export type ChatResponse = {
  channel: string
  module: string
  prompt: string
  reply: string
}

export function sendCh01Chat(prompt: string) {
  return postChannelRequest<ChatResponse>('/ch01/chat', { prompt })
}
