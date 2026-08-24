package com.smartpark.entity;

/**
 * Lifecycle status of a {@link ParkingSession}.
 */
public enum SessionStatus {
    /** Vehicle is currently checked in and occupying a space. */
    ACTIVE,
    /** Vehicle was checked out normally by a client call. */
    COMPLETED,
    /** Vehicle exceeded the maximum allowed parked duration and was removed automatically. */
    AUTO_REMOVED
}
