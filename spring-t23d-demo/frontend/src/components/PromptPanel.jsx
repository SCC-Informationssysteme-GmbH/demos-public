import { useState } from 'react'
import examplePrompts from '../data/examplePrompts.json'

function Icon({ path }) {
  return (
    <svg viewBox="0 0 24 24" width="15" height="15" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d={path} />
    </svg>
  )
}

const ICONS = {
  use: 'M3 10h10a8 8 0 0 1 8 8v2M3 10l6 6M3 10l6-6',
  delete: 'M3 6h18M8 6V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2m3 0v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6h14zM10 11v6M14 11v6',
}

function createHistoryEntry(text) {
  return { id: crypto.randomUUID(), text }
}

function PromptPanel({ onGenerate, loading, error }) {
  const [prompt, setPrompt] = useState('Eine Sechskantmutter mit M8-Innengewinde-Durchmesser')
  const [history, setHistory] = useState(() => examplePrompts.map(createHistoryEntry))

  const handleSubmit = (event) => {
    event.preventDefault()
    const trimmed = prompt.trim()
    if (!trimmed || loading) {
      return
    }
    setHistory((prev) => [createHistoryEntry(trimmed), ...prev.filter((entry) => entry.text !== trimmed)].slice(0, 10))
    onGenerate(trimmed)
  }

  function handleUseEntry(text) {
    setPrompt(text)
  }

  function handleDeleteEntry(id) {
    setHistory((prev) => prev.filter((entry) => entry.id !== id))
  }

  return (
    <div className="prompt-panel">
      <div className="prompt-panel-header">
        <span className="brand-badge">3D</span>
        <div>
          <h1>Text-to-3D (T23D)</h1>
          <p className="subtitle">Beschreibe ein Bauteil, ein Modell wird generiert</p>
        </div>
      </div>

      <form onSubmit={handleSubmit}>
        <textarea
          value={prompt}
          onChange={(event) => setPrompt(event.target.value)}
          placeholder="z. B. Ein Flansch mit 80mm Durchmesser und 4 Bohrungen"
          rows={8}
          disabled={loading}
        />
        <button type="submit" className="btn-primary" disabled={loading || !prompt.trim()}>
          {loading && <span className="spinner" />}
          {loading ? 'Generiere…' : 'Generieren'}
        </button>
      </form>

      {loading && <p className="status status-loading">Modell wird generiert…</p>}
      {error && <p className="status status-error">{error}</p>}

      {history.length > 0 && (
        <div className="history">
          <h2>Letzte Prompts</h2>
          <ul>
            {history.map((entry) => (
              <li key={entry.id} className="history-entry">
                <span className="history-entry-text" title={entry.text}>
                  {entry.text}
                </span>
                <div className="history-entry-actions">
                  <button
                    type="button"
                    className="icon-btn"
                    title="Prompt übernehmen"
                    aria-label="Prompt übernehmen"
                    onClick={() => handleUseEntry(entry.text)}
                    disabled={loading}
                  >
                    <Icon path={ICONS.use} />
                  </button>
                  <button
                    type="button"
                    className="icon-btn icon-btn-danger"
                    title="Aus Historie löschen"
                    aria-label="Aus Historie löschen"
                    onClick={() => handleDeleteEntry(entry.id)}
                  >
                    <Icon path={ICONS.delete} />
                  </button>
                </div>
              </li>
            ))}
          </ul>
        </div>
      )}
    </div>
  )
}

export default PromptPanel
