import { useEffect, useState } from 'react'
import {
  getTicket,
  listTickets,
  subscribeToTicket,
  type TicketView,
} from './api/ticketClient'
import { TicketForm } from './components/TicketForm'
import { TicketStatusStepper } from './components/TicketStatusStepper'
import { DraftReviewPanel } from './components/DraftReviewPanel'

export default function App() {
  const [tickets, setTickets] = useState<TicketView[]>([])
  const [selectedId, setSelectedId] = useState<string | null>(null)

  useEffect(() => {
    listTickets().then(setTickets).catch(() => undefined)
  }, [])

  // Live-Fortschritt: bei jedem Statuswechsel den vollen Ticket-Stand nachladen.
  useEffect(() => {
    if (!selectedId) return
    return subscribeToTicket(selectedId, () => {
      getTicket(selectedId).then(upsert).catch(() => undefined)
    })
  }, [selectedId])

  function upsert(ticket: TicketView) {
    setTickets((current) => {
      const rest = current.filter((t) => t.id !== ticket.id)
      return [ticket, ...rest]
    })
  }

  function onCreated(ticket: TicketView) {
    upsert(ticket)
    setSelectedId(ticket.id)
  }

  const selected = tickets.find((t) => t.id === selectedId) ?? null

  return (
    <main>
      <header>
        <h1>Multi-Agenten-Support</h1>
        <p>
          Klassifizierung &rarr; Recherche &rarr; Antwortentwurf, freigegeben durch
          einen Mitarbeiter.
        </p>
      </header>

      <TicketForm onCreated={onCreated} />

      <section className="card">
        <h2>Tickets</h2>
        {tickets.length === 0 && <p className="meta">Noch keine Anfragen.</p>}
        <ul className="tickets">
          {tickets.map((ticket) => (
            <li
              key={ticket.id}
              className={ticket.id === selectedId ? 'selected' : undefined}
              onClick={() => setSelectedId(ticket.id)}
            >
              <div className="ticket-text">{ticket.customerText}</div>
              <TicketStatusStepper state={ticket.state} />
            </li>
          ))}
        </ul>
      </section>

      {selected && <DraftReviewPanel ticket={selected} onDecided={upsert} />}
    </main>
  )
}
