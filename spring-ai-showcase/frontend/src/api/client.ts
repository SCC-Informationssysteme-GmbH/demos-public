export type ChannelStatusResponse = {
  channel: string
  module: string
  message: string
}

export async function getChannelStatus(path: string): Promise<ChannelStatusResponse> {
  const res = await fetch(`/api${path}`)
  if (!res.ok) {
    throw new Error(`Request fehlgeschlagen: ${res.status}`)
  }
  return res.json()
}

export async function postChannelRequest<T>(path: string, body: unknown): Promise<T> {
  const res = await fetch(`/api${path}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  })
  if (!res.ok) {
    throw new Error(`Request fehlgeschlagen: ${res.status}`)
  }
  return res.json()
}
