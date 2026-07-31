import { useState, useRef } from 'react'
import { synthesizeSpeech } from '../api/ttsApi'

const VOICES = ['alloy', 'echo', 'fable', 'onyx', 'nova', 'shimmer']
const MAX_TEXT_LENGTH = 4096

export function SingleTtsPage() {
  const [text, setText] = useState(
    'Bei KI-gestützte Text-to-Speech-Technologie wird künstliche Intelligenz genutzt, um geschriebenen Text in gesprochene Sprache umzuwandeln'
  )
  const [voice, setVoice] = useState('onyx')
  const [speed, setSpeed] = useState(1.1)
  const [instructions, setInstructions] = useState(
    'Sprich ruhig, sachlich und kompetent, wie ein erfahrener IT-Berater im Kundengespräch.'
  )
  const [loading, setLoading] = useState(false)
  const [playing, setPlaying] = useState(false)
  const [error, setError] = useState(null)
  const audioUrlRef = useRef(null)
  const audioRef = useRef(null)

  async function handleSpeak() {
    setError(null)
    setLoading(true)

    if (audioUrlRef.current) {
      URL.revokeObjectURL(audioUrlRef.current)
      audioUrlRef.current = null
    }

    try {
      const url = await synthesizeSpeech({ text, voice, speed, instructions })
      audioUrlRef.current = url

      if (audioRef.current) {
        audioRef.current.src = url
        await audioRef.current.play()
      }
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  function handleStop() {
    audioRef.current?.pause()
  }

  return (
    <div className="page tts-app">
      <div className="page-header">
        <h1>Text vorlesen</h1>
      </div>

      <div className="card tts-form">
        <div className="field">
          <label htmlFor="text">Text</label>
          <textarea
            id="text"
            value={text}
            onChange={(e) => setText(e.target.value)}
            placeholder="Text eingeben..."
            maxLength={MAX_TEXT_LENGTH}
            rows={8}
          />
          <span className="field-hint char-count">
            {text.length} / {MAX_TEXT_LENGTH}
          </span>
        </div>

        <div className="field">
          <label htmlFor="instructions">Anweisungen (optional)</label>
          <textarea
            id="instructions"
            value={instructions}
            onChange={(e) => setInstructions(e.target.value)}
            placeholder="z. B. 'sprich langsam und ruhig, wie ein Hörbuch'"
            maxLength={1024}
            rows={2}
          />
          <span className="field-hint">Nutzt automatisch das Modell gpt-4o-mini-tts</span>
        </div>

        <div className="field-row">
          <div className="field">
            <label htmlFor="voice">Stimme</label>
            <select id="voice" value={voice} onChange={(e) => setVoice(e.target.value)}>
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

        <div className="button-row">
          <button className="primary-button" onClick={handleSpeak} disabled={loading || !text.trim()}>
            {loading ? 'Generiere...' : 'Vorlesen'}
          </button>
          {(loading || playing) && (
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

        <audio
          ref={audioRef}
          controls
          onPlay={() => setPlaying(true)}
          onPause={() => setPlaying(false)}
          onEnded={() => setPlaying(false)}
        />
      </div>
    </div>
  )
}
