export async function generateModel(prompt) {
  const response = await fetch('/api/generate', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ prompt }),
  })

  if (!response.ok) {
    let message = `Generierung fehlgeschlagen (Status ${response.status})`
    try {
      const data = await response.json()
      if (data?.message) message = data.message
    } catch {
      // Antwort war kein JSON, Standardmeldung verwenden
    }
    throw new Error(message)
  }

  return response.json()
}
