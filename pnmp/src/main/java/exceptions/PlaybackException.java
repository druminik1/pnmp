package exceptions;

/**
 * custom exception thrown while playing back audio
 *
 * @author Dominik Scheffknecht 1426857
 */
public class PlaybackException extends Exception {

    public PlaybackException() {

    }

    public PlaybackException(String message) {
        super(message);
    }

    public PlaybackException(Throwable cause) {
        super(cause);
    }

    public PlaybackException(String message, Throwable cause) {
        super(message,cause);
    }

}
