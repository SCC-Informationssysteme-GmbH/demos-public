import { useEffect, useRef, useState, type FormEvent } from 'react'
import { askQuestion, getDocumentCount, getPersonas, type DocumentCount, type Persona } from '../api'
import { DocumentIcon, RedoIcon, TrashIcon } from './icons'

interface ChatEntry {
  id: number
  question: string
  answer: string
}

const QUESTION_MAX_LENGTH = 1000

export function ChatPanel() {
  const [question, setQuestion] = useState('')
  const [history, setHistory] = useState<ChatEntry[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [personas, setPersonas] = useState<Persona[]>([])
  const [persona, setPersona] = useState('')
  const [docCount, setDocCount] = useState<DocumentCount | null>(null)
  const nextId = useRef(0)

  useEffect(() => {
    getPersonas()
      .then((list) => {
        setPersonas(list)
        setPersona((current) => current || list[0]?.alias || '')
      })
      .catch(() => {
        // Dropdown bleibt leer, Backend faellt dann auf seinen Standard-Prompt zurueck
      })
    getDocumentCount()
      .then(setDocCount)
      .catch(() => {
        // Anzeige bleibt einfach aus, wenn die Zaehlung nicht verfuegbar ist
      })
  }, [])

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    const trimmed = question.trim()
    if (!trimmed || loading) return

    setLoading(true)
    setError(null)
    try {
      const response = await askQuestion(trimmed, persona || undefined)
      setHistory((prev) => [...prev, { id: nextId.current++, ...response }])
      setQuestion('')
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unbekannter Fehler')
    } finally {
      setLoading(false)
    }
  }

  function reuseEntry(entry: ChatEntry) {
    setQuestion(entry.question)
  }

  function removeEntry(id: number) {
    setHistory((prev) => prev.filter((entry) => entry.id !== id))
  }

  const activePersona = personas.find((p) => p.alias === persona)
  const historyNewestFirst = [...history].reverse()

  return (
    <section className="panel">
      <h2>Frage stellen</h2>
      <p className="panel-subtitle">
        Frage an das Modell stellen - je nach gewaehltem Verhalten mit oder ohne
        Rueckgriff auf die indexierten Dokumente.
      </p>
      <p className="notice">
        Einschraenkung dieser Demo: kein echter Chat. Jede Frage wird einzeln und
        ohne Gedaechtnis an vorherige Fragen beantwortet. Ausserdem gibt es keine
        Authentifizierung fuer die Verhaltens-Auswahl - jeder mit API-Zugriff
        kann jede Persona waehlen, unabhaengig vom Dropdown hier.
      </p>

      {docCount && (
        <div className="db-stat">
          <DocumentIcon />
          <span>
            {docCount.count} Chunk{docCount.count === 1 ? '' : 's'} in der Datenbank
            indexiert (Collection „{docCount.collection}“)
          </span>
        </div>
      )}

      {personas.length > 0 && (
        <div className="option-card">
          <p className="option-label">Verhalten</p>
          <div className="persona-select">
            <select
              id="persona"
              aria-label="Verhalten"
              value={persona}
              onChange={(e) => setPersona(e.target.value)}
            >
              {personas.map((p) => (
                <option key={p.alias} value={p.alias}>
                  {p.label}
                </option>
              ))}
            </select>
            {activePersona && <p className="persona-hint">{activePersona.systemPrompt}</p>}
          </div>
        </div>
      )}

      <div className="option-card">
        <p className="option-label">Frage</p>
        <form onSubmit={handleSubmit} className="chat-form">
          <textarea
            value={question}
            onChange={(e) => setQuestion(e.target.value)}
            placeholder="Was moechtest du wissen?"
            rows={4}
            maxLength={QUESTION_MAX_LENGTH}
            disabled={loading}
          />
          <p className="char-count">
            {question.length} / {QUESTION_MAX_LENGTH}
          </p>
          <button type="submit" disabled={loading || !question.trim()}>
            Fragen
          </button>
        </form>
        {error && <p className="error">{error}</p>}
        {loading && <p className="hint">Antwort wird generiert…</p>}
      </div>

      <div className="divider">
        <span>Verlauf</span>
      </div>

      <div className="history">
        {historyNewestFirst.length === 0 && <p className="hint">Noch keine Frage gestellt.</p>}
        {historyNewestFirst.map((entry) => (
          <div className="entry" key={entry.id}>
            <div className="entry-header">
              <p className="question">{entry.question}</p>
              <div className="entry-actions">
                <button
                  type="button"
                  className="ghost"
                  onClick={() => reuseEntry(entry)}
                  title="Als Frage uebernehmen"
                >
                  <RedoIcon />
                </button>
                <button
                  type="button"
                  className="ghost"
                  onClick={() => removeEntry(entry.id)}
                  title="Aus Verlauf loeschen"
                >
                  <TrashIcon />
                </button>
              </div>
            </div>
            <p className="answer">{entry.answer}</p>
          </div>
        ))}
      </div>
    </section>
  )
}
