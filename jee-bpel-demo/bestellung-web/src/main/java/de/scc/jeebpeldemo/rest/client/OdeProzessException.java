package de.scc.jeebpeldemo.rest.client;

/**
 * Wraps Fehler beim SOAP-Aufruf gegen den Apache-ODE-Prozessendpunkt, damit die
 * REST-Fassade sie unabhaengig von JAX-WS-Spezifika in HTTP-Statuscodes uebersetzen kann.
 */
public class OdeProzessException extends RuntimeException {

    public OdeProzessException(String message, Throwable cause) {
        super(message, cause);
    }
}
