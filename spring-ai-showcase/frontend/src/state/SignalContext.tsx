import { createContext, useContext, useState, type ReactNode } from 'react'

export type ChannelId = 'ch01' | 'ch02' | 'ch03' | 'ch04' | 'ch05' | 'ch06'
export type ChannelStatus = 'idle' | 'loading' | 'success' | 'error'

type SignalState = Record<ChannelId, ChannelStatus>

type SignalContextValue = {
  status: SignalState
  setChannelStatus: (channel: ChannelId, status: ChannelStatus) => void
}

const initialState: SignalState = {
  ch01: 'idle',
  ch02: 'idle',
  ch03: 'idle',
  ch04: 'idle',
  ch05: 'idle',
  ch06: 'idle',
}

const SignalContext = createContext<SignalContextValue | undefined>(undefined)

export function SignalProvider({ children }: { children: ReactNode }) {
  const [status, setStatus] = useState<SignalState>(initialState)

  const setChannelStatus = (channel: ChannelId, next: ChannelStatus) => {
    setStatus((prev) => ({ ...prev, [channel]: next }))
  }

  return (
    <SignalContext.Provider value={{ status, setChannelStatus }}>
      {children}
    </SignalContext.Provider>
  )
}

export function useSignal() {
  const ctx = useContext(SignalContext)
  if (!ctx) {
    throw new Error('useSignal must be used within SignalProvider')
  }
  return ctx
}
