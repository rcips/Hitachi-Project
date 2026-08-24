package com.smartpark.exception;

/** Thrown when login credentials do not match the configured static username/password. */
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
