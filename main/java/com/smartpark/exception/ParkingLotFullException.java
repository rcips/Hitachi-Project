package com.smartpark.exception;

/** Thrown when attempting to check a vehicle into a lot that has no available spaces. */
public class ParkingLotFullException extends RuntimeException {
    public ParkingLotFullException(String message) {
        super(message);
    }
}
