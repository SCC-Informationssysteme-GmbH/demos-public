import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { useSignal } from '../../../state/SignalContext'
import ChannelLog from '../../../components/ChannelLog/ChannelLog'
import { fetchCh03Documents, sendCh03Ask } from '../../../api/ch03Rag'
import type { RagDocumentSummary } from '../../../api/ch03Rag'
import type { ChannelStatus } from '../../../state/SignalContext'

const EXAMPLE_QUESTION = 'Wie viele Urlaubstage stehen mir zu?'

export default function Ch03Page() {
  const { setChannelStatus } = useSignal()
  const [documents, setDocuments] = useState<RagDocumentSummary[]>([])
  const [question, setQuestion] = useState(EXAMPLE_QUESTION)
  const [status, setStatus] = useState<ChannelStatus>('idle')
  const [output, setOutput] = useState<string>()

  useEffect(() => {
    fetchCh03Documents()
      .then(setDocuments)
      .catch(() => setStatus('error'))
  }, [])

  const run = async () => {
    setStatus('loading')
    setChannelStatus('ch03', 'loading')
    try {
      const data = await sendCh03Ask(question)
      const sourceList = data.sources.map((source) => `- ${source.title}`).join('\n')
      setOutput(`Quellen:\n${sourceList}\n\nAntwort:\n${data.answer}`)
      setStatus('success')
      setChannelStatus('ch03', 'success')
    } catch {
      setStatus('error')
      setChannelStatus('ch03', 'error')
    }
  }

  return (
    <section>
      <Link to="/" className="back-link">&larr; Patch-Panel</Link>
      <h2>CH.03 — RAG</h2>
      <p className="hint">Wissensbasis: {documents.map((doc) => doc.title).join(', ')}</p>
      <textarea
        className="prompt-input"
        value={question}
        onChange={(e) => setQuestion(e.target.value)}
        rows={3}
      />
      <button onClick={run}>Anfrage senden</button>
      <ChannelLog status={status} output={output} />
    </section>
  )
}
