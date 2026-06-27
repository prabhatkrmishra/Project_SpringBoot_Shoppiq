package com.pkmprojects.shoppiq.exception;

public class InvalidOidcUserException extends RuntimeException {

    public InvalidOidcUserException(String message) {
        super(message);
    }

    public InvalidOidcUserException(String message, Throwable cause) {
        super(message, cause);
    }
}
