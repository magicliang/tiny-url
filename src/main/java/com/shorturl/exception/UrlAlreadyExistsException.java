package com.shorturl.exception;

/**
 * Exception thrown when trying to create a URL that already exists
 */
public class UrlAlreadyExistsException extends RuntimeException {

    public UrlAlreadyExistsException(String message) {
        super(message);
    }

    public UrlAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}