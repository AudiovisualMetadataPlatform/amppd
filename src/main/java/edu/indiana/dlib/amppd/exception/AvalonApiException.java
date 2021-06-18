package edu.indiana.dlib.amppd.exception;

public class AvalonApiException extends RuntimeException {

    public AvalonApiException(String message) {
        super(message);
    }

    public AvalonApiException(String message, Throwable cause) {
        super(message, cause);
    }

}