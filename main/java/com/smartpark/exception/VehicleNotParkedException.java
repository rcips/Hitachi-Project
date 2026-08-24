package com.smartpark.exception;

/** Thrown when trying to check out a vehicle that has no active parking session. */
public class VehicleNotParkedException extends RuntimeException {
    public VehicleNotParkedException(String message) {
        super(message);
    }
}
