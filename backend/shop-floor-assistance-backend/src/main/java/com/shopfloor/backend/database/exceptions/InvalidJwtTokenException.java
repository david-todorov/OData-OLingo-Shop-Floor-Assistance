package com.shopfloor.backend.database.exceptions;

/**
 * Exception thrown when the JWT token is invalid or expired.
 * @author David Todorov (https://github.com/david-todorov)
 */
public class InvalidJwtTokenException extends RuntimeException {
    private static final String MESSAGE = "Invalid or expired JWT token";

    /**
     * Constructs a new InvalidJwtTokenException with a default message.
     */
    public InvalidJwtTokenException() {
        super(MESSAGE);
    }
}
