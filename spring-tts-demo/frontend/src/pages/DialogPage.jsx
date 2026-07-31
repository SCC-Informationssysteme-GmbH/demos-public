import { useRef, useState } from 'react'
import { synthesizeSpeech } from '../api/ttsApi'
import dialogPreset from '../data/dialogPreset.json'

const VOICES = ['alloy', 'echo', 'fable', 'onyx', 'nova', 'shimmer']
const MAX_LINE_LENGTH = 4096

function voiceForEntry(entry, voice1, voice2) {
  return entry.voice === 2 ? voice2 : voice1
}

class DialogStopped extends Error {}

export function DialogPage() {
  const [voice1, setVoice1] = useState('onyx')
  const [voice2, setVoice2] = useState('nova')
  const [speed, setSpeed] = useState(1.0)
  const [entries, setEntries] = useState(
    dialogPreset.map((entry, index) => ({ id: index + 1, voice: entry.voice, text: entry.text }))
  )
  const [playingIndex, setPlayingIndex] = useState(-1)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)
  const nextIdRef = useRef(dialogPreset.length + 1)
  const audioUrlRef = useRef(null)
  const audioRef = useRef(null)
  const stopPlaybackRef = useRef(null)

  function addEntry() {
    const lastVoice = entries.length > 0 ? entries[entries.length - 1].voice : 2
    const nextVoice = lastVoice === 1 ? 2 : 1
    setEntries([...entries, { id: nextIdRef.current++, voice: nextVoice, text: '' }])
  }

  function removeEntry(id) {
    setEntries(entries.filter((entry) => entry.id !== id))
  }

  function updateEntryText(id, text) {
    setEntries(entries.map((entry) => (entry.id === id ? { ...entry, text } : entry)))
  }

  function playAudio(url) {
    return new Promise((resolve, reject) => {
      const audio = audioRef.current
      audio.src = url

      function cleanup() {
        audio.removeEventListener('ended', onEnded)
        audio.removeEventListener('error', onError)
        stopPlaybackRef.current = null
      }
      function onEnded() {
        cleanup()
        resolve()
      }
      function onError() {
        cleanup()
        reject(new Error('Audiowiedergabe fehlgeschlagen'))
      }

      stopPlaybackRef.current = () => {
        cleanup()
        audio.pause()
        reject(new DialogStopped())
      }

      audio.addEventListener('ended', onEnded)
      audio.addEventListener('error', onError)
      audio.play().catch(reject)
    })
  }

  async function handlePlayAll() {
    setError(null)
    setLoading(true)

    try {
      for (let i = 0; i < entries.length; i++) {
        const entry = entries[i]
        if (!entry.text.trim()) continue

        setPlayingIndex(i)

        if (audioUrlRef.current) {
          URL.revokeObjectURL(audioUrlRef.current)
        }
        const url = await synthesizeSpeech({ text: entry.text, voice: voiceForEntry(entry, voice1, voice2), speed })
        audioUrlRef.current = url
        await playAudio(url)
      }
    } catch (err) {
      if (!(err instanceof DialogStopped)) {
        setError(err.message)
      }
    } finally {
      setLoading(false)
      setPlayingIndex(-1)
    }
  }

  function handleStop() {
    stopPlaybackRef.current?.()
  }

  const hasText = entries.some((entry) => entry.text.trim())

  return (
    <div className="page dialog-app">
      <div className="page-header">
        <h1>Dialog</h1>
      </div>

      <div className="card tts-form">
        <div className="field-row">
          <div className="field">
            <label htmlFor="voice1">Stimme 1</label>
            <select id="voice1" value={voice1} onChange={(e) => setVoice1(e.target.value)}>
              {VOICES.map((v) => (
                <option key={v} value={v}>
                  {v}
                </option>
              ))}
            </select>
          </div>

          <div className="field">
            <label htmlFor="voice2">Stimme 2</label>
            <select id="voice2" value={voice2} onChange={(e) => setVoice2(e.target.value)}>
              {VOICES.map((v) => (
                <option key={v} value={v}>
                  {v}
                </option>
              ))}
            </select>
          </div>

          <div className="field">
            <label htmlFor="speed">Tempo</label>
            <div className="speed-field">
              <input
                id="speed"
                type="range"
                min="0.25"
                max="4"
                step="0.05"
                value={speed}
                onChange={(e) => setSpeed(Number(e.target.value))}
              />
              <span className="speed-value">{speed.toFixed(2)}x</span>
            </div>
          </div>
        </div>

        <div className="stack">
          {entries.map((entry, index) => (
            <div key={entry.id} className={`stack-entry ${playingIndex === index ? 'active' : ''}`}>
              <span className={`voice-tag voice-tag-${entry.voice}`}>Stimme {entry.voice}</span>
              <div className="stack-entry-text">
                <textarea
                  value={entry.text}
                  onChange={(e) => updateEntryText(entry.id, e.target.value)}
                  placeholder="Text für diese Zeile..."
                  maxLength={MAX_LINE_LENGTH}
                  rows={2}
                />
                <span className="field-hint char-count">
                  {entry.text.length} / {MAX_LINE_LENGTH}
                </span>
              </div>
              <button
                type="button"
                className="icon-button"
                aria-label="Zeile entfernen"
                onClick={() => removeEntry(entry.id)}
              >
                ×
              </button>
            </div>
          ))}
        </div>

        <button type="button" className="secondary-button" onClick={addEntry}>
          + Zeile hinzufügen
        </button>

        <div className="button-row">
          <button className="primary-button" onClick={handlePlayAll} disabled={loading || !hasText}>
            {loading ? 'Läuft...' : 'Dialog abspielen'}
          </button>
          {loading && (
            <button type="button" className="secondary-button" onClick={handleStop}>
              Stop
            </button>
          )}
        </div>

        {error && (
          <div className="banner error" role="alert">
            {error}
          </div>
        )}

        <audio ref={audioRef} controls />
      </div>
    </div>
  )
}
