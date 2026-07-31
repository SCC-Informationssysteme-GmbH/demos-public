import { useSignal, type ChannelId } from '../../state/SignalContext'
import styles from './SignalPath.module.css'

const CHANNELS: ChannelId[] = ['ch01', 'ch02', 'ch03', 'ch04', 'ch05', 'ch06']

export default function SignalPath() {
  const { status } = useSignal()

  return (
    <div className={styles.path}>
      {CHANNELS.map((ch) => (
        <span
          key={ch}
          className={`${styles.node} ${styles[status[ch]]}`}
          title={ch.toUpperCase()}
        />
      ))}
    </div>
  )
}
