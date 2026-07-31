import { getChannelStatus, postChannelRequest } from './client'

export type TicketClassification = {
  category: 'BILLING' | 'TECHNICAL' | 'ACCOUNT' | 'GENERAL'
  priority: 'LOW' | 'MEDIUM' | 'HIGH'
  summary: string
  suggestedReply: string
}

export type TicketClassificationResponse = {
  channel: string
  module: string
  ticketText: string
  classification: TicketClassification
}

export function fetchCh06Status() {
  return getChannelStatus('/ch06/status')
}

export function sendCh06Classify(ticketText: string) {
  return postChannelRequest<TicketClassificationResponse>('/ch06/classify', { ticketText })
}
