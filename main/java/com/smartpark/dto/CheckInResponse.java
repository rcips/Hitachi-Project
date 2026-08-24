package com.smartpark.dto;

import java.time.LocalDateTime;

public class CheckInResponse {

    private Long sessionId;
    private String lotId;
    private String licensePlate;
    private LocalDateTime checkInTime;
    private int availableSpacesRemaining;

    public CheckInResponse() {
    }

    public CheckInResponse(Long sessionId, String lotId, String licensePlate,
                            LocalDateTime checkInTime, int availableSpacesRemaining) {
        this.sessionId = sessionId;
        this.lotId = lotId;
        this.licensePlate = licensePlate;
        this.checkInTime = checkInTime;
        this.availableSpacesRemaining = availableSpacesRemaining;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public String getLotId() {
        return lotId;
    }

    public void setLotId(String lotId) {
        this.lotId = lotId;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public LocalDateTime getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(LocalDateTime checkInTime) {
        this.checkInTime = checkInTime;
    }

    public int getAvailableSpacesRemaining() {
        return availableSpacesRemaining;
    }

    public void setAvailableSpacesRemaining(int availableSpacesRemaining) {
        this.availableSpacesRemaining = availableSpacesRemaining;
    }
}
