import { useState } from 'react'
import { ChatPanel } from './components/ChatPanel'
import { GlossaryPanel } from './components/GlossaryPanel'
import { IngestPanel } from './components/IngestPanel'
import { BookIcon, ChatIcon, CpuIcon, DocumentIcon } from './components/icons'
import './App.css'

type View = 'chat' | 'ingest' | 'glossary'

const NAV_ITEMS: { id: View; label: string; icon: typeof ChatIcon }[] = [
  { id: 'chat', label: 'Frage stellen', icon: ChatIcon },
  { id: 'ingest', label: 'Dokumente indexieren', icon: DocumentIcon },
  { id: 'glossary', label: 'Glossar', icon: BookIcon },
]

function App() {
  const [view, setView] = useState<View>('chat')

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">
          <span className="brand-mark" aria-hidden="true">
            <CpuIcon />
          </span>
          <div>
            <p className="brand-title">Lokales LLM Demo</p>
            <p className="brand-sub">RAG mit Spring AI, Ollama &amp; Qdrant</p>
          </div>
        </div>
        <nav className="nav">
          {NAV_ITEMS.map(({ id, label, icon: Icon }) => (
            <button
              key={id}
              type="button"
              className={id === view ? 'nav-item active' : 'nav-item'}
              onClick={() => setView(id)}
            >
              <Icon />
              <span>{label}</span>
            </button>
          ))}
        </nav>
      </aside>
      <main className="content">
        <div className="content-inner">
          {view === 'chat' && <ChatPanel />}
          {view === 'ingest' && <IngestPanel />}
          {view === 'glossary' && <GlossaryPanel />}
        </div>
      </main>
    </div>
  )
}

export default App
