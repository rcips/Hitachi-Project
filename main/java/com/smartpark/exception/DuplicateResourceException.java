package com.smartpark.exception;

/** Thrown when attempting to register a lot/vehicle whose unique identifier already exists. */
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
