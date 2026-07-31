import { useRef } from 'react'

export function InfoDialog() {
  const dialogRef = useRef(null)

  return (
    <>
      <button
        type="button"
        className="icon-button"
        aria-label="Was ist AI TTS?"
        onClick={() => dialogRef.current?.showModal()}
      >
        <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" strokeWidth="2">
          <circle cx="12" cy="12" r="9" />
          <line x1="12" y1="11" x2="12" y2="16.5" />
          <circle cx="12" cy="7.5" r="0.75" fill="currentColor" stroke="none" />
        </svg>
      </button>

      <dialog ref={dialogRef} className="info-dialog">
        <h2>Was ist AI TTS?</h2>
        <p>
          <strong>TTS</strong> steht für <strong>Text-to-Speech</strong> (Text-zu-Sprache). Bei <strong>AI TTS</strong>{' '}
          wandelt künstliche Intelligenz geschriebenen Text in gesprochene Sprache um – meist mit sehr natürlich
          klingenden, menschenähnlichen Stimmen.
        </p>

        <h3>Vorteile gegenüber klassischem TTS</h3>
        <p>Ältere TTS-Systeme klingen oft robotisch. KI-Modelle bilden dagegen realistischer nach:</p>
        <ul>
          <li>Betonung</li>
          <li>Sprachmelodie</li>
          <li>Emotionen</li>
        </ul>

        <h3>Typische Anwendungen</h3>
        <ul>
          <li>Vorlesefunktionen (Hörbücher, Artikel, Barrierefreiheit)</li>
          <li>Sprachassistenten (Siri, Alexa, Google Assistant)</li>
          <li>Synchronisation/Voiceover für Videos</li>
          <li>Navigationsansagen</li>
        </ul>

        <button type="button" className="secondary-button" onClick={() => dialogRef.current?.close()}>
          Schließen
        </button>
      </dialog>
    </>
  )
}
