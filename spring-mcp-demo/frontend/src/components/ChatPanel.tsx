import { useState } from 'react'
import { sendChatMessage } from '../api'

interface ChatMessage {
  role: 'user' | 'assistant' | 'error'
  content: string
}

const EXAMPLES = ['Welche Buecher gibt es?', 'Welche Bestellungen hat Kunde 1?', 'Welche Kunden gibt es?']

const AVATARS: Record<ChatMessage['role'], string> = {
  user: '🧑',
  assistant: '🤖',
  error: '⚠️',
}

export default function ChatPanel() {
  const [messages, setMessages] = useState<ChatMessage[]>([])
  const [input, setInput] = useState('')
  const [loading, setLoading] = useState(false)

  async function send(message: string) {
    if (!message || loading) return
    setMessages((prev) => [...prev, { role: 'user', content: message }])
    setInput('')
    setLoading(true)
    try {
      const { reply } = await sendChatMessage(message)
      setMessages((prev) => [...prev, { role: 'assistant', content: reply }])
    } catch (err) {
      setMessages((prev) => [
        ...prev,
        { role: 'error', content: err instanceof Error ? err.message : 'Unbekannter Fehler' },
      ])
    } finally {
      setLoading(false)
    }
  }

  function handleSubmit(event: React.FormEvent) {
    event.preventDefault()
    void send(input.trim())
  }

  return (
    <div className="panel">
      {messages.length === 0 ? (
        <div className="empty-state">
          <span className="logo" aria-hidden="true">
            💬
          </span>
          <p className="hint">Frag etwas zu Büchern, Bestellungen oder Kunden.</p>
          <div className="chips">
            {EXAMPLES.map((example) => (
              <button key={example} type="button" className="chip" onClick={() => void send(example)}>
                {example}
              </button>
            ))}
          </div>
        </div>
      ) : (
        <div className="messages">
          {messages.map((msg, i) => (
            <div key={i} className={`row ${msg.role}`}>
              <span className="avatar" aria-hidden="true">
                {AVATARS[msg.role]}
              </span>
              <div className="bubble">{msg.content}</div>
            </div>
          ))}
          {loading && (
            <div className="row assistant">
              <span className="avatar" aria-hidden="true">
                🤖
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
        </div>
      )}
      <form className="composer" onSubmit={handleSubmit}>
        <input
          value={input}
          onChange={(e) => setInput(e.target.value)}
          placeholder="Nachricht eingeben..."
          disabled={loading}
        />
        <button className="send-button" type="submit" disabled={loading || !input.trim()} aria-label="Senden">
          <svg width="16" height="16" viewBox="0 0 16 16" fill="none" aria-hidden="true">
            <path d="M1 8h13M9 2l6 6-6 6" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round" />
          </svg>
        </button>
      </form>
    </div>
  )
}
