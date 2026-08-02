import { getChannelStatus, postChannelRequest } from './client'

export type PromptTemplateSummary = {
  key: string
  label: string
}

export type PromptChatResponse = {
  channel: string
  module: string
  templateKey: string
  renderedPrompt: string
  reply: string
}

export function fetchCh02Status() {
  return getChannelStatus('/ch02/status')
}

export async function fetchCh02Templates(): Promise<PromptTemplateSummary[]> {
  const res = await fetch('/api/ch02/templates')
  if (!res.ok) {
    throw new Error(`Request fehlgeschlagen: ${res.status}`)
  }
  return res.json()
}

export function sendCh02Chat(templateKey: string, input: string) {
  return postChannelRequest<PromptChatResponse>('/ch02/chat', { templateKey, input })
}
