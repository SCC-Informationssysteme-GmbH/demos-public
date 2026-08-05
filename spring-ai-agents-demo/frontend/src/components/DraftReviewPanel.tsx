import { useEffect, useState } from 'react'
import { approveTicket, type TicketView } from '../api/ticketClient'

interface Props {
  ticket: TicketView
  onDecided: (ticket: TicketView) => void
}

/**
 * Mitarbeiter-Ansicht: Klassifizierung, Quellen und editierbarer Entwurf.
 * Der Mensch ist kein vierter Agent, sondern ein expliziter Freigabe-Schritt.
 */
export function DraftReviewPanel({ ticket, onDecided }: Props) {
  const [text, setText] = useState(ticket.draft?.text ?? '')
  const [busy, setBusy] = useState(false)

  useEffect(() => {
    setText(ticket.draft?.text ?? '')
  }, [ticket.id, ticket.draft?.text])

  // LOGGED und ESCALATED erzeugen ihren Text aus einer Vorlage, ohne Freigabe.
  const istAutomatisch = ticket.state === 'LOGGED' || ticket.state === 'ESCALATED'

  async function decide(approved: boolean) {
    setBusy(true)
    try {
      onDecided(await approveTicket(ticket.id, approved, approved ? text : undefined))
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="card">
      <h2>Mitarbeiter-Freigabe</h2>

      {ticket.classification && (
        <p className="meta">
          Kategorie <strong>{ticket.classification.category}</strong>{' '}
          (confidence {ticket.classification.confidence.toFixed(2)}) &middot; Keywords:{' '}
          {ticket.classification.keywords.join(', ')}
        </p>
      )}

      {ticket.research && (
        <details open>
          <summary>Rechercheergebnis</summary>
          <p>{ticket.research.summary}</p>
          <ul>
            {ticket.research.sources.map((source, i) => (
              <li key={i}>
                <strong>{source.title}</strong>: {source.snippet}
              </li>
            ))}
          </ul>
        </details>
      )}

      {ticket.state === 'AWAITING_APPROVAL' && ticket.draft ? (
        <>
          <p className="meta">
            Entwurf-confidence {ticket.draft.confidence.toFixed(2)} - vor dem Versand pruefen.
          </p>
          <textarea rows={10} value={text} onChange={(e) => setText(e.target.value)} />
          <div className="row">
            <button disabled={busy} onClick={() => decide(true)}>
              Freigeben und versenden
            </button>
            <button className="ghost" disabled={busy} onClick={() => decide(false)}>
              Ablehnen
            </button>
          </div>
        </>
      ) : (
        <p className="meta">
          Kein Entwurf zur Freigabe (Zustand: {ticket.state}).
          {ticket.state === 'ESCALATED' &&
            ' Die Anfrage liegt bei der Fachabteilung.'}
        </p>
      )}

      {ticket.finalText && (
        <details open={istAutomatisch}>
          <summary>
            {istAutomatisch
              ? 'Automatische Eingangsbestaetigung (Vorlage, ohne Freigabe versendet)'
              : 'Versendeter Text (nach Freigabe)'}
          </summary>
          <pre>{ticket.finalText}</pre>
        </details>
      )}
    </div>
  )
}
