export interface AskResponse {
  question: string
  answer: string
}

export interface IngestResponse {
  chunksIndexed: number
  source?: string
}

export interface Persona {
  alias: string
  label: string
  systemPrompt: string
}

export interface DocumentCount {
  collection: string
  count: number
}

async function parseJsonOrThrow<T>(response: Response): Promise<T> {
  if (!response.ok) {
    const text = await response.text().catch(() => '')
    throw new Error(`HTTP ${response.status}: ${text || response.statusText}`)
  }
  return response.json() as Promise<T>
}

export function askQuestion(question: string, persona?: string): Promise<AskResponse> {
  return fetch('/api/chat/ask', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ question, persona }),
  }).then((res) => parseJsonOrThrow<AskResponse>(res))
}

export function getPersonas(): Promise<Persona[]> {
  return fetch('/api/chat/personas').then((res) => parseJsonOrThrow<Persona[]>(res))
}

export function ingestText(content: string, source: string): Promise<IngestResponse> {
  return fetch('/api/documents', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ content, source }),
  }).then((res) => parseJsonOrThrow<IngestResponse>(res))
}

export function ingestFile(file: File): Promise<IngestResponse> {
  const formData = new FormData()
  formData.append('file', file)
  return fetch('/api/documents/upload', {
    method: 'POST',
    body: formData,
  }).then((res) => parseJsonOrThrow<IngestResponse>(res))
}

export function getDocumentCount(): Promise<DocumentCount> {
  return fetch('/api/documents/count').then((res) => parseJsonOrThrow<DocumentCount>(res))
}
