package com.smartpark.exception;

/** Thrown when trying to check in a vehicle that already has an active parking session. */
public class VehicleAlreadyParkedException extends RuntimeException {
    public VehicleAlreadyParkedException(String message) {
        super(message);
    }
}
