package edu.indiana.dlib.amppd.exception;

public class GalaxyWorkflowException extends RuntimeException {

    public GalaxyWorkflowException(String message) {
        super(message);
    }

    public GalaxyWorkflowException(String message, Throwable cause) {
        super(message, cause);
    }