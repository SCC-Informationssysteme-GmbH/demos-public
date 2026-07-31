import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { useSignal } from '../../../state/SignalContext'
import ChannelLog from '../../../components/ChannelLog/ChannelLog'
import { fetchCh02Templates, sendCh02Chat } from '../../../api/ch02Prompt'
import type { PromptTemplateSummary } from '../../../api/ch02Prompt'
import type { ChannelStatus } from '../../../state/SignalContext'

const EXAMPLE_INPUT = 'Das Produkt kam pünktlich an, aber die Verpackung war beschädigt.'

export default function Ch02Page() {
  const { setChannelStatus } = useSignal()
  const [templates, setTemplates] = useState<PromptTemplateSummary[]>([])
  const [templateKey, setTemplateKey] = useState('')
  const [input, setInput] = useState(EXAMPLE_INPUT)
  const [status, setStatus] = useState<ChannelStatus>('idle')
  const [output, setOutput] = useState<string>()

  useEffect(() => {
    fetchCh02Templates()
      .then((data) => {
        setTemplates(data)
        setTemplateKey(data[0]?.key ?? '')
      })
      .catch(() => setStatus('error'))
  }, [])

  const run = async () => {
    setStatus('loading')
    setChannelStatus('ch02', 'loading')
    try {
      const data = await sendCh02Chat(templateKey, input)
      setOutput(`Template-Prompt:\n${data.renderedPrompt}\n\nAntwort:\n${data.reply}`)
      setStatus('success')
      setChannelStatus('ch02', 'success')
    } catch {
      setStatus('error')
      setChannelStatus('ch02', 'error')
    }
  }

  return (
    <section>
      <Link to="/" className="back-link">&larr; Patch-Panel</Link>
      <h2>CH.02 — Prompt-Orchestrierung</h2>
      <select value={templateKey} onChange={(e) => setTemplateKey(e.target.value)}>
        {templates.map((template) => (
          <option key={template.key} value={template.key}>{template.label}</option>
        ))}
      </select>
      <textarea
        className="prompt-input"
        value={input}
        onChange={(e) => setInput(e.target.value)}
        rows={3}
      />
      <button onClick={run} disabled={!templateKey}>Anfrage senden</button>
      <ChannelLog status={status} output={output} />
    </section>
  )
}
