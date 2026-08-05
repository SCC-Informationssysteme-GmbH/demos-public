import { useState } from 'react'
import { createTicket, type TicketView } from '../api/ticketClient'

interface Props {
  onCreated: (ticket: TicketView) => void
}

const BEISPIELE = [
  'Ich kann mich seit heute Morgen nicht mehr anmelden, es kommt immer "Sitzung abgelaufen".',
  'Wie lange laeuft mein Vertrag noch und bis wann muesste ich kuendigen?',
  'Es waere super, wenn man Rechnungen als CSV exportieren koennte.',
]

export function TicketForm({ onCreated }: Props) {
  const [text, setText] = useState('')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function submit(event: React.FormEvent) {
    event.preventDefault()
    if (!text.trim()) return
    setBusy(true)
    setError(null)
    try {
      onCreated(await createTicket(text.trim()))
      setText('')
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Unbekannter Fehler')
    } finally {
      setBusy(false)
    }
  }

  return (
    <form className="card" onSubmit={submit}>
      <h2>Neue Anfrage</h2>
      <textarea
        value={text}
        onChange={(e) => setText(e.target.value)}
        rows={5}
        placeholder="Beschreiben Sie Ihr Anliegen..."
      />
      <div className="row">
        <button type="submit" disabled={busy || !text.trim()}>
          {busy ? 'Wird gesendet...' : 'Anfrage senden'}
        </button>
        {BEISPIELE.map((beispiel, i) => (
          <button
            key={i}
            type="button"
            className="ghost"
            onClick={() => setText(beispiel)}
          >
            Beispiel {i + 1}
          </button>
        ))}
      </div>
      {error && <p className="error">{error}</p>}
    </form>
  )
}
