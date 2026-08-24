package com.smartpark.dto;

import com.smartpark.entity.VehicleType;

import java.time.LocalDateTime;

public class ParkedVehicleResponse {

    private String licensePlate;
    private VehicleType type;
    private String ownerName;
    private LocalDateTime checkInTime;
    private long minutesParkedSoFar;

    public ParkedVehicleResponse() {
    }

    public ParkedVehicleResponse(String licensePlate, VehicleType type, String ownerName,
                                  LocalDateTime checkInTime, long minutesParkedSoFar) {
        this.licensePlate = licensePlate;
        this.type = type;
        this.ownerName = ownerName;
        this.checkInTime = checkInTime;
        this.minutesParkedSoFar = minutesParkedSoFar;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public VehicleType getType() {
        return type;
    }

    public void setType(VehicleType type) {
        this.type = type;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public LocalDateTime getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(LocalDateTime checkInTime) {
        this.checkInTime = checkInTime;
    }

    public long getMinutesParkedSoFar() {
        return minutesParkedSoFar;
    }

    public void setMinutesParkedSoFar(long minutesParkedSoFar) {
        this.minutesParkedSoFar = minutesParkedSoFar;
    }
}
