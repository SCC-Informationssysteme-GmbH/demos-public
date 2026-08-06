interface GlossaryEntry {
  term: string
  definition: string
}

const ENTRIES: GlossaryEntry[] = [
  {
    term: 'Chunk / Chunking',
    definition:
      'Text wird beim Indexieren in kleinere Abschnitte (hier ca. 800 Token) zerlegt, weil Embedding-Modelle und Vektorsuche besser mit kurzen, thematisch fokussierten Abschnitten arbeiten als mit ganzen Dokumenten.',
  },
  {
    term: 'Embedding',
    definition:
      'Numerische Vektor-Darstellung von Text, die dessen Bedeutung erfasst. Aehnliche Inhalte liegen im Vektorraum nah beieinander - das macht semantische Suche moeglich.',
  },
  {
    term: 'Frontmatter',
    definition:
      'Ein Metadaten-Block am Anfang eines Dokuments (z.B. "--- title: ... ---" in Markdown), der Infos wie Titel oder Tags vom eigentlichen Inhalt trennt. Wichtig fuer diese Demo: Beim Indexieren wird er nicht als Metadaten erkannt und herausgefiltert, sondern ganz normal mitgelesen, in Chunks zerlegt und mit eingebettet - genau wie der restliche Text. Es gibt also keine automatische Trennung zwischen "Metadaten" und "Inhalt".',
  },
  {
    term: 'Halluzination',
    definition:
      'Wenn ein Sprachmodell eine plausibel klingende, aber falsche oder erfundene Antwort gibt. Kleine lokale Modelle wie llama3.2 3B neigen dazu, vor allem ohne passenden Kontext.',
  },
  {
    term: 'LLM (Large Language Model)',
    definition:
      'Ein auf riesigen Textmengen trainiertes KI-Modell, das Sprache versteht und generiert. In dieser Demo laeuft es lokal ueber Ollama statt in der Cloud.',
  },
  {
    term: 'Ollama',
    definition:
      'Werkzeug, um LLMs (Chat- und Embedding-Modelle) lokal auf dem eigenen Rechner laufen zu lassen, ueber eine einheitliche REST-API.',
  },
  {
    term: 'Persona / Verhalten',
    definition:
      'In dieser Demo ein austauschbarer System-Prompt-Alias (z.B. "streng nur Kontext" oder "knapp & direkt"), der steuert, wie das Modell mit dem Retrieval-Kontext umgeht.',
  },
  {
    term: 'Qdrant',
    definition:
      'Der in dieser Demo verwendete Vektorspeicher, in dem die Embeddings der indexierten Text-Chunks abgelegt und durchsucht werden.',
  },
  {
    term: 'RAG (Retrieval-Augmented Generation)',
    definition:
      'Technik, bei der vor der Antwort passende Textausschnitte aus einer eigenen Wissensbasis gesucht und dem Prompt als Kontext mitgegeben werden - damit das Modell auf Basis eigener Dokumente statt nur seines Trainingswissens antwortet.',
  },
  {
    term: 'Similarity Threshold',
    definition:
      'Mindest-Aehnlichkeitswert (0-1), ab dem ein Chunk bei der Vektorsuche als relevant genug gilt, um in den Kontext aufgenommen zu werden.',
  },
  {
    term: 'Spring AI',
    definition:
      'Java/Spring-Erweiterung, die LLM-Anbieter (Ollama, OpenAI, ...), Vektorspeicher und RAG-Bausteine (Advisor) einheitlich ansprechbar macht.',
  },
  {
    term: 'System-Prompt',
    definition:
      'Die Verhaltensanweisung, die dem Modell vor jeder Nutzerfrage mitgegeben wird und festlegt, wie es sich verhalten soll (z.B. Ton, Umgang mit fehlendem Kontext).',
  },
  {
    term: 'Temperature',
    definition:
      'Steuert, wie "kreativ"/zufaellig das Modell antwortet. Niedrige Werte (hier: 0.3) liefern eher deterministische, vorhersehbare Antworten.',
  },
  {
    term: 'Top-K',
    definition:
      'Anzahl der aehnlichsten Chunks, die bei der Vektorsuche maximal zurueckgegeben werden (hier: 4).',
  },
  {
    term: 'Vektorspeicher (Vector Store)',
    definition:
      'Datenbank, spezialisiert auf das Speichern und schnelle Durchsuchen von Embeddings (hier: Qdrant) - Basis fuer die semantische Suche in RAG.',
  },
]

export function GlossaryPanel() {
  return (
    <section className="panel">
      <h2>Glossar</h2>
      <p className="panel-subtitle">
        Die wichtigsten Begriffe rund um diese Demo, kurz erklaert.
      </p>

      <div className="glossary-list">
        {ENTRIES.map((entry) => (
          <div className="glossary-item" key={entry.term}>
            <p className="term">{entry.term}</p>
            <p className="definition">{entry.definition}</p>
          </div>
        ))}
      </div>
    </section>
  )
}
