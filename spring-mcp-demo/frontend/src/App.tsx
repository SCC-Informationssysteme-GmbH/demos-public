import { useState } from 'react'
import ChatPanel from './components/ChatPanel'
import AgentPanel from './components/AgentPanel'
import './App.css'

type Mode = 'chat' | 'agent'

const NAV_ITEMS: { mode: Mode; label: string; icon: string; subtitle: string }[] = [
  {
    mode: 'chat',
    label: 'Chat',
    icon: '💬',
    subtitle: 'Tool-Calling-Chat über den mcp-orchestrator-backend',
  },
  {
    mode: 'agent',
    label: 'Agent',
    icon: '🕵️',
    subtitle: 'Mehrschrittige Aufgaben mit sichtbaren Zwischenschritten',
  },
]

function App() {
  const [mode, setMode] = useState<Mode>('chat')
  const current = NAV_ITEMS.find((item) => item.mode === mode)!

  return (
    <div className="app">
      <aside className="sidebar">
        <div className="brand">
          <span className="logo" aria-hidden="true">
            📚
          </span>
          <span className="brand-name">MCP Buchhandlung Demo</span>
        </div>
        <nav className="nav">
          {NAV_ITEMS.map((item) => (
            <button
              key={item.mode}
              className={mode === item.mode ? 'active' : ''}
              onClick={() => setMode(item.mode)}
            >
              <span className="icon" aria-hidden="true">
                {item.icon}
              </span>
              {item.label}
            </button>
          ))}
        </nav>

        <div className="sidebar-note">
          <strong>Hinweis:</strong> Summen/Gesamtausgaben (auch mit Zeitraum, z.&nbsp;B.
          "im März") berechnet ein serverseitiges Tool (<code>get_total_spent</code>) —
          zuverlässig statt vom Modell selbst zusammengerechnet. Für alles andere (z.&nbsp;B.
          "meistverkauftes Buch") trägt das Modell weiterhin Rohdaten selbst zusammen.
          Außerdem hat der Chat kein Gedächtnis: jede Nachricht wird isoliert beantwortet.
        </div>
      </aside>

      <div className="main">
        <header>
          <h2>
            <span className="icon" aria-hidden="true">
              {current.icon}
            </span>
            {current.label}
          </h2>
          <p className="subtitle">{current.subtitle}</p>
        </header>
        {mode === 'chat' ? <ChatPanel /> : <AgentPanel />}
      </div>
    </div>
  )
}

export default App
