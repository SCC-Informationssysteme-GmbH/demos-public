import { useState } from 'react'
import { runAgentTask, type AgentResult } from '../api'

const EXAMPLES = [
  'Wie viel Geld hat Kunde 1 insgesamt ausgegeben und welche Buecher waren das?',
  'Welches ist das teuerste bestellte Buch und wer hat es gekauft?',
]

export default function AgentPanel() {
  const [task, setTask] = useState('')
  const [result, setResult] = useState<AgentResult | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function run(value: string) {
    if (!value || loading) return
    setTask(value)
    setLoading(true)
    setError(null)
    setResult(null)
    try {
      setResult(await runAgentTask(value))
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unbekannter Fehler')
    } finally {
      setLoading(false)
    }
  }

  function handleSubmit(event: React.FormEvent) {
    event.preventDefault()
    void run(task.trim())
  }

  return (
    <div className="panel">
      {!result && !loading && !error && (
        <div className="empty-state">
          <span className="logo" aria-hidden="true">
            🕵️
          </span>
          <p className="hint">
            Mehrschrittige, auch domänenübergreifende Aufgaben — der Agent zeigt seine
            Zwischenschritte.
          </p>
          <div className="chips">
            {EXAMPLES.map((example) => (
              <button key={example} type="button" className="chip" onClick={() => void run(example)}>
                {example}
              </button>
            ))}
          </div>
        </div>
      )}

      {loading && (
        <div className="row assistant">
          <span className="avatar" aria-hidden="true">
            🕵️
          </span>
          <div className="bubble">
            <span className="typing">
              <span />
              <span />
              <span />
            </span>
          </div>
        </div>
      )}

      {error && (
        <div className="row error">
          <span className="avatar" aria-hidden="true">
            ⚠️
          </span>
          <div className="bubble">{error}</div>
        </div>
      )}

      {result && (
        <div className="agent-result">
          <h3>Antwort</h3>
          <div className="row assistant">
            <span className="avatar" aria-hidden="true">
              🕵️
            </span>
            <div className="bubble">{result.answer}</div>
          </div>

          <h3>Zwischenschritte ({result.steps.length})</h3>
          {result.steps.length === 0 && <p className="hint">Keine Tool-Aufrufe notwendig.</p>}
          <ol className="timeline">
            {result.steps.map((step, i) => (
              <li key={i}>
                <span className="step-index">{i + 1}</span>
                <div className="step-body">
                  <span className="step-tool">{step.tool}</span>
                  <details>
                    <summary>Eingabe / Ausgabe anzeigen</summary>
                    <div className="step-io">
                      <span>
                        Eingabe:
                        <code>{step.input}</code>
                      </span>
                      <span>
                        Ausgabe:
                        <code>{step.output}</code>
                      </span>
                    </div>
                  </details>
                </div>
              </li>
            ))}
          </ol>
        </div>
      )}

      <form className="composer" onSubmit={handleSubmit}>
        <input
          value={task}
          onChange={(e) => setTask(e.target.value)}
          placeholder="Aufgabe eingeben..."
          disabled={loading}
        />
        <button className="send-button" type="submit" disabled={loading || !task.trim()} aria-label="Ausführen">
          <svg width="16" height="16" viewBox="0 0 16 16" fill="none" aria-hidden="true">
            <path d="M1 8h13M9 2l6 6-6 6" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round" />
          </svg>
        </button>
      </form>
    </div>
  )
}
