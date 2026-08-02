import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useSignal } from '../../../state/SignalContext'
import ChannelLog from '../../../components/ChannelLog/ChannelLog'
import { sendCh04Search, triggerCh04Index } from '../../../api/ch04VectorDb'
import type { ChannelStatus } from '../../../state/SignalContext'

const EXAMPLE_QUERY = 'Kann ich von zuhause arbeiten?'

export default function Ch04Page() {
  const { setChannelStatus } = useSignal()
  const [query, setQuery] = useState(EXAMPLE_QUERY)
  const [status, setStatus] = useState<ChannelStatus>('idle')
  const [output, setOutput] = useState<string>()

  const index = async () => {
    setStatus('loading')
    setChannelStatus('ch04', 'loading')
    try {
      const data = await triggerCh04Index()
      setOutput(`${data.indexedCount} Dokumente in Qdrant indexiert.`)
      setStatus('success')
      setChannelStatus('ch04', 'success')
    } catch {
      setStatus('error')
      setChannelStatus('ch04', 'error')
    }
  }

  const search = async () => {
    setStatus('loading')
    setChannelStatus('ch04', 'loading')
    try {
      const data = await sendCh04Search(query)
      const matchList = data.matches
        .map((match) => `- ${match.title} (Score: ${match.score.toFixed(3)})`)
        .join('\n')
      setOutput(`Treffer:\n${matchList}`)
      setStatus('success')
      setChannelStatus('ch04', 'success')
    } catch {
      setStatus('error')
      setChannelStatus('ch04', 'error')
    }
  }

  return (
    <section>
      <Link to="/" className="back-link">&larr; Patch-Panel</Link>
      <h2>CH.04 — Vektordatenbank</h2>
      <p className="hint">Backend: Qdrant (REST-API, Collection "ai-demo-docs")</p>
      <button onClick={index}>Dokumente indexieren</button>
      <textarea
        className="prompt-input"
        value={query}
        onChange={(e) => setQuery(e.target.value)}
        rows={2}
      />
      <button onClick={search}>Suche starten</button>
      <ChannelLog status={status} output={output} />
    </section>
  )
}
