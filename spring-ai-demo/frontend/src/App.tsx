import { Routes, Route } from 'react-router-dom'
import { SignalProvider } from './state/SignalContext'
import SignalPath from './components/SignalPath/SignalPath'
import PatchPanel from './pages/PatchPanel'
import Ch01Page from './pages/channels/ch01-llmrest/Ch01Page'
import Ch02Page from './pages/channels/ch02-prompt/Ch02Page'
import Ch03Page from './pages/channels/ch03-rag/Ch03Page'
import Ch04Page from './pages/channels/ch04-vectordb/Ch04Page'
import Ch05Page from './pages/channels/ch05-langchain/Ch05Page'
import Ch06Page from './pages/channels/ch06-businesslogic/Ch06Page'

export default function App() {
  return (
    <SignalProvider>
      <div className="app-shell">
        <header className="app-header">
          <h1>Signal-Konsole</h1>
          <SignalPath />
        </header>
        <main>
          <Routes>
            <Route path="/" element={<PatchPanel />} />
            <Route path="/ch/01" element={<Ch01Page />} />
            <Route path="/ch/02" element={<Ch02Page />} />
            <Route path="/ch/03" element={<Ch03Page />} />
            <Route path="/ch/04" element={<Ch04Page />} />
            <Route path="/ch/05" element={<Ch05Page />} />
            <Route path="/ch/06" element={<Ch06Page />} />
          </Routes>
        </main>
      </div>
    </SignalProvider>
  )
}
