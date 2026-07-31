import { Routes, Route, NavLink } from "react-router-dom";
import { NewOrderPage } from "./pages/NewOrderPage";
import { HistoryPage } from "./pages/HistoryPage";
import "./App.css";

function App() {
  return (
    <>
      <nav className="sidebar">
        <div className="brand">Spring n8n Demo</div>
        <NavLink to="/" end className={({ isActive }) => (isActive ? "nav-link active" : "nav-link")}>
          Neue Bestellung
        </NavLink>
        <NavLink to="/historie" className={({ isActive }) => (isActive ? "nav-link active" : "nav-link")}>
          Historie
        </NavLink>
      </nav>
      <main className="content">
        <Routes>
          <Route path="/" element={<NewOrderPage />} />
          <Route path="/historie" element={<HistoryPage />} />
        </Routes>
      </main>
    </>
  );
}

export default App;
