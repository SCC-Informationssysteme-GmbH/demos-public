import { Link } from 'react-router-dom'
import styles from './ChannelSlot.module.css'

type Props = {
  id: string
  title: string
  module: string
}

export default function ChannelSlot({ id, title, module }: Props) {
  return (
    <Link to={`/ch/${id}`} className={styles.slot}>
      <span className={styles.code}>CH.{id}</span>
      <span className={styles.title}>{title}</span>
      <span className={styles.module}>{module}</span>
    </Link>
  )
}
