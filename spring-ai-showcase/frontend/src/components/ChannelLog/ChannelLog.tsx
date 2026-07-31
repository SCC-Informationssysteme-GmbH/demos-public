import type { ChannelStatus } from '../../state/SignalContext'

type Props = {
  status: ChannelStatus
  output?: string
}

export default function ChannelLog({ status, output }: Props) {
  return (
    <pre className="channel-log">
      {status === 'idle' && '// bereit'}
      {status === 'loading' && '// sende Anfrage...'}
      {status === 'success' && output}
      {status === 'error' && '// Fehler beim Request'}
    </pre>
  )
}
