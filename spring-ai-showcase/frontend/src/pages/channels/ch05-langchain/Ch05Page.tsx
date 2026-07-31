import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useSignal } from '../../../state/SignalContext'
import ChannelLog from '../../../components/ChannelLog/ChannelLog'
import { sendCh05Chat } from '../../../api/ch05LangChain'
import type { ChannelStatus } from '../../../state/SignalContext'

const EXAMPLE_MESSAGE = 'Hallo, mein Name ist Max Mustermann. Ich habe ein Problem mit dem Login.'

export default function Ch05Page() {
  const { setChannelStatus } = useSignal()
  const [sessionId] = useState(() => crypto.randomUUID())
  const [message, setMessage] = useState(EXAMPLE_MESSAGE)
  const [history, setHistory] = useState<string[]>([])
  const [status, setStatus] = useState<ChannelStatus>('idle')

  const run = async () => {
    setStatus('loading')
    setChannelStatus('ch05', 'loading')
    try {
      const data = await sendCh05Chat(sessionId, message)
      setHistory((prev) => [...prev, `> ${message}`, data.reply])
      setStatus('success')
      setChannelStatus('ch05', 'success')
    } catch {
      setStatus('error')
      setChannelStatus('ch05', 'error')
    }
  }

  return (
    <section>
      <Link to="/" className="back-link">&larr; Patch-Panel</Link>
      <h2>CH.05 — LangChain4J</h2>
      <p className="hint">Session {sessionId.slice(0, 8)} — Gesprächsverlauf bleibt über mehrere Nachrichten erhalten</p>
      <textarea
        className="prompt-input"
        value={message}
        onChange={(e) => setMessage(e.target.value)}
        rows={2}
      />
      <button onClick={run}>Nachricht senden</button>
      <ChannelLog status={status} output={history.join('\n\n')} />
    </section>
  )
}
