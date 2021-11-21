package de.tum.i13.client.exception;

public class ConnectionException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs an {@code ClientException} with the specified detail message.
     *
     * @param message the detail message for this exception.
     */
    public ConnectionException(String message) {
        super(message);
    }

    /**
     * Constructs an {@code ClientException} with the specified detail message and
     * the exception that caused it.
     *
     * @param message the detail message for this exception.
     * @param e       the exception that caused this exception.
     */
    public ConnectionException(String message, Exception e) {
        super(message, e);
    }

}