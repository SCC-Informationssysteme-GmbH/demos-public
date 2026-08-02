import { getChannelStatus, postChannelRequest } from './client'

export type VectorMatch = {
  docId: string
  title: string
  score: number
}

export type VectorDbIndexResponse = {
  channel: string
  module: string
  indexedCount: number
}

export type VectorDbSearchResponse = {
  channel: string
  module: string
  query: string
  matches: VectorMatch[]
}

export function fetchCh04Status() {
  return getChannelStatus('/ch04/status')
}

export function triggerCh04Index() {
  return postChannelRequest<VectorDbIndexResponse>('/ch04/index', {})
}

export function sendCh04Search(query: string) {
  return postChannelRequest<VectorDbSearchResponse>('/ch04/search', { query })
}
