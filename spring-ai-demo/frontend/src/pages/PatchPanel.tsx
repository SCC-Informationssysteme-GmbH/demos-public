import ChannelSlot from '../components/ChannelSlot/ChannelSlot'

const CHANNELS = [
  { id: '01', title: 'LLM per REST', module: 'llmrest' },
  { id: '02', title: 'Prompt-Orchestrierung', module: 'prompt' },
  { id: '03', title: 'RAG', module: 'rag' },
  { id: '04', title: 'Vektordatenbank', module: 'vectordb' },
  { id: '05', title: 'LangChain4J', module: 'langchain' },
  { id: '06', title: 'KI-Business-Logik', module: 'businesslogic' },
]

export default function PatchPanel() {
  return (
    <div className="patch-panel">
      {CHANNELS.map((ch) => (
        <ChannelSlot key={ch.id} id={ch.id} title={ch.title} module={ch.module} />
      ))}
    </div>
  )
}
