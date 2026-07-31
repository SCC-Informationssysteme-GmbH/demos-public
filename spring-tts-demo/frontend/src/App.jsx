import { NavLink, Route, Routes } from 'react-router-dom'
import { SingleTtsPage } from './pages/SingleTtsPage'
import { DialogPage } from './pages/DialogPage'
import { InfoDialog } from './components/InfoDialog'
import './App.css'

function App() {
  return (
    <>
      <nav className="sidebar">
        <div className="brand-row">
          <span className="brand">Text-to-Speech (TTS)</span>
          <InfoDialog />
        </div>
        <NavLink to="/" end className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}>
          Text vorlesen
        </NavLink>
        <NavLink to="/dialog" className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}>
          Dialog
        </NavLink>
      </nav>
      <main className="content">
        <Routes>
          <Route path="/" element={<SingleTtsPage />} />
          <Route path="/dialog" element={<DialogPage />} />
        </Routes>
      </main>
    </>
  )
}

export default App
