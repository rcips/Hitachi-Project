package com.smartpark.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CheckOutResponse {

    private Long sessionId;
    private String lotId;
    private String licensePlate;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private long minutesParked;
    private BigDecimal cost;

    public CheckOutResponse() {
    }

    public CheckOutResponse(Long sessionId, String lotId, String licensePlate, LocalDateTime checkInTime,
                             LocalDateTime checkOutTime, long minutesParked, BigDecimal cost) {
        this.sessionId = sessionId;
        this.lotId = lotId;
        this.licensePlate = licensePlate;
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
        this.minutesParked = minutesParked;
        this.cost = cost;
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

    public LocalDateTime getCheckOutTime() {
        return checkOutTime;
    }

    public void setCheckOutTime(LocalDateTime checkOutTime) {
        this.checkOutTime = checkOutTime;
    }

    public long getMinutesParked() {
        return minutesParked;
    }

    public void setMinutesParked(long minutesParked) {
        this.minutesParked = minutesParked;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }
}
