package exceptions;

/**
 * custom exception thrown while persisting noiseprofiles
 *
 * @author Dominik Scheffknecht 1426857
 */
public class PersistenceException extends Exception{

    public PersistenceException() {

    }

    public PersistenceException(String message) {
        super(message);
    }

    public PersistenceException(Throwable cause) {
        super(cause);
    }

    public PersistenceException(String message, Throwable cause) {
        super(message,cause);
    }
}
