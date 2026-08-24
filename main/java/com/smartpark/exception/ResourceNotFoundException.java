package com.smartpark.exception;

/** Thrown when a requested parking lot, vehicle or session cannot be found. */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
