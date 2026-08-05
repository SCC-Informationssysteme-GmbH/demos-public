import type { TicketState } from '../api/ticketClient'

interface Props {
  state: TicketState
}

const SCHRITTE: { state: TicketState; label: string }[] = [
  { state: 'NEW', label: 'Eingegangen' },
  { state: 'CLASSIFIED', label: 'Eingeordnet' },
  { state: 'RESEARCHED', label: 'Recherchiert' },
  { state: 'AWAITING_APPROVAL', label: 'Entwurf erstellt' },
  { state: 'SENT', label: 'Versendet' },
]

const ENDZUSTAENDE: Partial<Record<TicketState, string>> = {
  LOGGED: 'Ins Backlog aufgenommen - Eingangsbestaetigung versendet',
  ESCALATED: 'An die Fachabteilung eskaliert - Eingangsbestaetigung versendet',
  REJECTED: 'Abgelehnt bzw. abgebrochen',
}

export function TicketStatusStepper({ state }: Props) {
  const abschluss = ENDZUSTAENDE[state]
  if (abschluss) {
    return (
      <div className="stepper">
        <span className="badge done">{abschluss}</span>
      </div>
    )
  }

  const aktuellerIndex = SCHRITTE.findIndex((s) => s.state === state)

  return (
    <ol className="stepper">
      {SCHRITTE.map((schritt, i) => {
        const status =
          i < aktuellerIndex ? 'done' : i === aktuellerIndex ? 'active' : 'todo'
        return (
          <li key={schritt.state} className={status}>
            {schritt.label}
          </li>
        )
      })}
    </ol>
  )
}
