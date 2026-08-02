import { useCallback, useRef, useState } from 'react'
import PromptPanel from './components/PromptPanel'
import ModelViewer from './components/ModelViewer'
import { generateModel } from './api/generateApi'
import './App.css'

const MIN_LEFT_WIDTH = 280
const MAX_LEFT_WIDTH = 640
const DEFAULT_LEFT_WIDTH = 340

function App() {
  const [modelUrl, setModelUrl] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)
  const [leftWidth, setLeftWidth] = useState(DEFAULT_LEFT_WIDTH)
  const draggingRef = useRef(false)

  const handleGenerate = async (prompt) => {
    setLoading(true)
    setError(null)
    try {
      const result = await generateModel(prompt)
      setModelUrl(result.modelUrl)
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  const handleSplitterPointerDown = useCallback((event) => {
    event.preventDefault()
    draggingRef.current = true
    document.body.style.cursor = 'col-resize'

    const handlePointerMove = (moveEvent) => {
      if (!draggingRef.current) return
      const next = Math.min(MAX_LEFT_WIDTH, Math.max(MIN_LEFT_WIDTH, moveEvent.clientX))
      setLeftWidth(next)
    }
    const handlePointerUp = () => {
      draggingRef.current = false
      document.body.style.cursor = ''
      window.removeEventListener('pointermove', handlePointerMove)
      window.removeEventListener('pointerup', handlePointerUp)
    }

    window.addEventListener('pointermove', handlePointerMove)
    window.addEventListener('pointerup', handlePointerUp)
  }, [])

  return (
    <div className="app-layout" style={{ gridTemplateColumns: `${leftWidth}px 6px minmax(0, 1fr)` }}>
      <PromptPanel onGenerate={handleGenerate} loading={loading} error={error} />
      <div
        className="splitter"
        onPointerDown={handleSplitterPointerDown}
        role="separator"
        aria-orientation="vertical"
        aria-label="Breite des Prompt-Bereichs anpassen"
      />
      <div className="viewer-scroll">
        <div className="viewer-scroll-inner">
          <ModelViewer modelUrl={modelUrl} />
        </div>
      </div>
    </div>
  )
}

export default App
