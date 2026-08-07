export interface AgentStep {
  tool: string
  input: string
  output: string
}

export interface AgentResult {
  answer: string
  steps: AgentStep[]
}

async function postJson<T>(url: string, body: unknown): Promise<T> {
  const response = await fetch(url, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  })
  if (!response.ok) {
    throw new Error(`${url} antwortete mit Status ${response.status}`)
  }
  return response.json() as Promise<T>
}

export function sendChatMessage(message: string): Promise<{ reply: string }> {
  return postJson('/api/chat', { message })
}

export function runAgentTask(task: string): Promise<AgentResult> {
  return postJson('/api/agent', { task })
}
