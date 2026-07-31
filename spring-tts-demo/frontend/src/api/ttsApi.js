export async function synthesizeSpeech({ text, voice, speed, instructions }) {
  const response = await fetch('/api/tts', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ text, voice, speed, instructions: instructions?.trim() || null }),
  })

  if (!response.ok) {
    const message = await response.text()
    throw new Error(message || `Fehler ${response.status}`)
  }

  const blob = await response.blob()
  return URL.createObjectURL(blob)
}
