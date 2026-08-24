package com.smartpark.dto;

import java.math.BigDecimal;

public class ParkingLotResponse {

    private String lotId;
    private String location;
    private int capacity;
    private int occupiedSpaces;
    private int availableSpaces;
    private BigDecimal costPerMinute;

    public ParkingLotResponse() {
    }

    public ParkingLotResponse(String lotId, String location, int capacity, int occupiedSpaces,
                               int availableSpaces, BigDecimal costPerMinute) {
        this.lotId = lotId;
        this.location = location;
        this.capacity = capacity;
        this.occupiedSpaces = occupiedSpaces;
        this.availableSpaces = availableSpaces;
        this.costPerMinute = costPerMinute;
    }

    public String getLotId() {
        return lotId;
    }

    public void setLotId(String lotId) {
        this.lotId = lotId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getOccupiedSpaces() {
        return occupiedSpaces;
    }

    public void setOccupiedSpaces(int occupiedSpaces) {
        this.occupiedSpaces = occupiedSpaces;
    }

    public int getAvailableSpaces() {
        return availableSpaces;
    }

    public void setAvailableSpaces(int availableSpaces) {
        this.availableSpaces = availableSpaces;
    }

    public BigDecimal getCostPerMinute() {
        return costPerMinute;
    }

    public void setCostPerMinute(BigDecimal costPerMinute) {
        this.costPerMinute = costPerMinute;
    }
}
