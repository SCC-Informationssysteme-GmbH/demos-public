import { useRef, useState, type DragEvent, type FormEvent } from 'react'
import { ingestFile, ingestText } from '../api'
import { UploadIcon } from './icons'

export function IngestPanel() {
  const [content, setContent] = useState('')
  const [source, setSource] = useState('')
  const [status, setStatus] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)
  const [dragOver, setDragOver] = useState(false)
  const [fileName, setFileName] = useState<string | null>(null)
  const fileInputRef = useRef<HTMLInputElement>(null)

  async function handleTextSubmit(event: FormEvent) {
    event.preventDefault()
    const trimmed = content.trim()
    if (!trimmed || loading) return

    setLoading(true)
    setError(null)
    setStatus(null)
    try {
      const result = await ingestText(trimmed, source.trim() || 'manual')
      setStatus(`${result.chunksIndexed} Chunk(s) indexiert.`)
      setContent('')
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unbekannter Fehler')
    } finally {
      setLoading(false)
    }
  }

  async function handleFile(file: File) {
    setLoading(true)
    setError(null)
    setStatus(null)
    setFileName(file.name)
    try {
      const result = await ingestFile(file)
      setStatus(`${result.chunksIndexed} Chunk(s) aus "${result.source}" indexiert.`)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unbekannter Fehler')
    } finally {
      setLoading(false)
      if (fileInputRef.current) fileInputRef.current.value = ''
    }
  }

  function handleFileChange(event: React.ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0]
    if (file) handleFile(file)
  }

  function handleDrop(event: DragEvent<HTMLDivElement>) {
    event.preventDefault()
    setDragOver(false)
    if (loading) return
    const file = event.dataTransfer.files?.[0]
    if (file) handleFile(file)
  }

  return (
    <section className="panel">
      <h2>Dokument indexieren</h2>
      <p className="panel-subtitle">
        Text oder Datei einspeisen, damit der Assistent spaeter darauf zugreifen kann.
      </p>
      <div className="option-card">
        <p className="option-label">Weg 1 &middot; Text einfuegen</p>
        <form onSubmit={handleTextSubmit} className="ingest-form">
          <textarea
            value={content}
            onChange={(e) => setContent(e.target.value)}
            placeholder="Text zum Indexieren einfuegen…"
            rows={5}
            disabled={loading}
          />
          <input
            type="text"
            value={source}
            onChange={(e) => setSource(e.target.value)}
            placeholder="Quelle (optional)"
            disabled={loading}
          />
          <button type="submit" disabled={loading || !content.trim()}>
            Text indexieren
          </button>
        </form>
        <p className="field-hint">
          Reiner Text - kein Parsing von YAML-Frontmatter (--- title: ... ---) oder
          sonstigen strukturierten Feldern.
        </p>
      </div>

      <div className="divider">
        <span>oder</span>
      </div>

      <div className="option-card">
        <p className="option-label">Weg 2 &middot; Datei hochladen</p>
        <div
          className={`dropzone${dragOver ? ' drag-over' : ''}${loading ? ' disabled' : ''}`}
          onDragOver={(e) => {
            e.preventDefault()
            if (!loading) setDragOver(true)
          }}
          onDragLeave={() => setDragOver(false)}
          onDrop={handleDrop}
        >
          <UploadIcon />
          <p>
            <label htmlFor="file-upload" className="dropzone-label">
              Datei auswaehlen
            </label>{' '}
            oder hierher ziehen
          </p>
          <p className="dropzone-hint">.txt oder .md</p>
          {fileName && <p className="dropzone-file">{fileName}</p>}
          <input
            id="file-upload"
            ref={fileInputRef}
            type="file"
            accept=".txt,.md"
            onChange={handleFileChange}
            disabled={loading}
            className="visually-hidden"
          />
        </div>
      </div>

      {status && <p className="status">{status}</p>}
      {error && <p className="error">{error}</p>}
    </section>
  )
}
