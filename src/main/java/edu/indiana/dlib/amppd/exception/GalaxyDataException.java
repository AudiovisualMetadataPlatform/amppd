package edu.indiana.dlib.amppd.exception;

public class GalaxyDataException extends StorageException {

    public GalaxyDataException(String message) {
        super(message);
    }

    public GalaxyDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
