import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useSignal } from '../../../state/SignalContext'
import ChannelLog from '../../../components/ChannelLog/ChannelLog'
import { sendCh01Chat } from '../../../api/ch01LlmRest'
import type { ChannelStatus } from '../../../state/SignalContext'

const EXAMPLE_PROMPT = 'Erkläre in zwei Sätzen, was Retrieval Augmented Generation ist.'

export default function Ch01Page() {
  const { setChannelStatus } = useSignal()
  const [prompt, setPrompt] = useState(EXAMPLE_PROMPT)
  const [status, setStatus] = useState<ChannelStatus>('idle')
  const [output, setOutput] = useState<string>()

  const run = async () => {
    setStatus('loading')
    setChannelStatus('ch01', 'loading')
    try {
      const data = await sendCh01Chat(prompt)
      setOutput(data.reply)
      setStatus('success')
      setChannelStatus('ch01', 'success')
    } catch {
      setStatus('error')
      setChannelStatus('ch01', 'error')
    }
  }

  return (
    <section>
      <Link to="/" className="back-link">&larr; Patch-Panel</Link>
      <h2>CH.01 — LLM per REST</h2>
      <textarea
        className="prompt-input"
        value={prompt}
        onChange={(e) => setPrompt(e.target.value)}
        rows={3}
      />
      <button onClick={run}>Anfrage senden</button>
      <ChannelLog status={status} output={output} />
    </section>
  )
}
