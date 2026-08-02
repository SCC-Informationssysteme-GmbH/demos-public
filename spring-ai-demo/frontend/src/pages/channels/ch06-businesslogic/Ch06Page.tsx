import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useSignal } from '../../../state/SignalContext'
import ChannelLog from '../../../components/ChannelLog/ChannelLog'
import { sendCh06Classify } from '../../../api/ch06BusinessLogic'
import type { ChannelStatus } from '../../../state/SignalContext'

const EXAMPLE_TICKET =
  'Seit dem Update kann ich mich überhaupt nicht mehr einloggen, die App stürzt sofort ab. ' +
  'Das ist sehr dringend, ich brauche den Zugang heute noch für eine Präsentation.'

export default function Ch06Page() {
  const { setChannelStatus } = useSignal()
  const [ticketText, setTicketText] = useState(EXAMPLE_TICKET)
  const [status, setStatus] = useState<ChannelStatus>('idle')
  const [output, setOutput] = useState<string>()

  const run = async () => {
    setStatus('loading')
    setChannelStatus('ch06', 'loading')
    try {
      const data = await sendCh06Classify(ticketText)
      const { category, priority, summary, suggestedReply } = data.classification
      setOutput(
        `Kategorie: ${category}\nPriorität: ${priority}\nZusammenfassung: ${summary}\n\nAntwortentwurf:\n${suggestedReply}`
      )
      setStatus('success')
      setChannelStatus('ch06', 'success')
    } catch {
      setStatus('error')
      setChannelStatus('ch06', 'error')
    }
  }

  return (
    <section>
      <Link to="/" className="back-link">&larr; Patch-Panel</Link>
      <h2>CH.06 — KI-Business-Logik</h2>
      <p className="hint">Support-Ticket-Triage: Kategorie, Priorität und Antwortentwurf per LLM</p>
      <textarea
        className="prompt-input"
        value={ticketText}
        onChange={(e) => setTicketText(e.target.value)}
        rows={4}
      />
      <button onClick={run}>Ticket klassifizieren</button>
      <ChannelLog status={status} output={output} />
    </section>
  )
}
