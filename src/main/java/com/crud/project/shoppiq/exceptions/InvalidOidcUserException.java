package com.crud.project.shoppiq.exceptions;

public class InvalidOidcUserException extends RuntimeException {

    public InvalidOidcUserException(String message) {
        super(message);
    }

    public InvalidOidcUserException(String message, Throwable cause) {
        super(message, cause);
    }
}
