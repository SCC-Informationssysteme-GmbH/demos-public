export type TicketState =
  | 'NEW'
  | 'CLASSIFIED'
  | 'RESEARCHED'
  | 'AWAITING_APPROVAL'
  | 'LOGGED'
  | 'ESCALATED'
  | 'SENT'
  | 'REJECTED'

export type Category =
  | 'TECHNISCHES_PROBLEM'
  | 'VERTRAGSFRAGE'
  | 'FEATURE_WUNSCH'
  | 'SONSTIGES'

export interface SourceRef {
  title: string
  snippet: string
}

export interface ClassificationResult {
  category: Category
  confidence: number
  keywords: string[]
}

export interface ResearchResult {
  summary: string
  sources: SourceRef[]
}

export interface DraftAnswer {
  text: string
  sources: SourceRef[]
  confidence: number
}

export interface TicketView {
  id: string
  customerText: string
  state: TicketState
  classification: ClassificationResult | null
  research: ResearchResult | null
  draft: DraftAnswer | null
  finalText: string | null
  createdAt: string
  updatedAt: string
}

export interface TicketStatusEvent {
  ticketId: string
  state: TicketState
  at: string
}

async function json<T>(response: Response): Promise<T> {
  if (!response.ok) {
    throw new Error(`${response.status} ${response.statusText}`)
  }
  return (await response.json()) as T
}

export function createTicket(text: string): Promise<TicketView> {
  return fetch('/api/tickets', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ text }),
  }).then(json<TicketView>)
}

export function getTicket(id: string): Promise<TicketView> {
  return fetch(`/api/tickets/${id}`).then(json<TicketView>)
}

export function listTickets(): Promise<TicketView[]> {
  return fetch('/api/tickets').then(json<TicketView[]>)
}

export function approveTicket(
  id: string,
  approved: boolean,
  editedText?: string,
): Promise<TicketView> {
  return fetch(`/api/tickets/${id}/approve`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ approved, editedText: editedText ?? null }),
  }).then(json<TicketView>)
}

/**
 * Abonniert die Live-Statuswechsel eines Tickets. Rueckgabewert schliesst die Verbindung.
 */
export function subscribeToTicket(
  id: string,
  onStatus: (event: TicketStatusEvent) => void,
): () => void {
  const source = new EventSource(`/api/tickets/${id}/stream`)
  source.addEventListener('status', (event) => {
    onStatus(JSON.parse((event as MessageEvent).data) as TicketStatusEvent)
  })
  source.onerror = () => source.close()
  return () => source.close()
}
