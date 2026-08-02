import { getChannelStatus, postChannelRequest } from './client'

export type RagDocumentSummary = {
  id: string
  title: string
}

export type RagAskResponse = {
  channel: string
  module: string
  question: string
  sources: RagDocumentSummary[]
  answer: string
}

export function fetchCh03Status() {
  return getChannelStatus('/ch03/status')
}

export async function fetchCh03Documents(): Promise<RagDocumentSummary[]> {
  const res = await fetch('/api/ch03/documents')
  if (!res.ok) {
    throw new Error(`Request fehlgeschlagen: ${res.status}`)
  }
  return res.json()
}

export function sendCh03Ask(question: string) {
  return postChannelRequest<RagAskResponse>('/ch03/ask', { question })
}
