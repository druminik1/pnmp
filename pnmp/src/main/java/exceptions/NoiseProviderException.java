package exceptions;

/**
 * custom exception thrown while loading noises in the noiseprovider
 *
 * @author Dominik Scheffknecht 1426857
 */
public class NoiseProviderException extends Exception{

    public NoiseProviderException() {

    }

    public NoiseProviderException(String message) {
        super(message);
    }

    public NoiseProviderException(Throwable cause) {
        super(cause);
    }

    public NoiseProviderException(String message, Throwable cause) {
        super(message,cause);
    }
}
