package com.smartpark.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * A physical parking lot managed by SmartPark.
 *
 * The lotId itself acts as the unique business identifier (natural key),
 * as required by the specification (max 50 characters).
 */
@Entity
@Table(name = "parking_lot")
public class ParkingLot {

    @Id
    @Column(name = "lot_id", length = 50, nullable = false, updatable = false)
    private String lotId;

    @Column(name = "location", nullable = false)
    private String location;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @Column(name = "occupied_spaces", nullable = false)
    private Integer occupiedSpaces = 0;

    @Column(name = "cost_per_minute", nullable = false, precision = 10, scale = 2)
    private BigDecimal costPerMinute;

    public ParkingLot() {
    }

    public ParkingLot(String lotId, String location, Integer capacity, BigDecimal costPerMinute) {
        this.lotId = lotId;
        this.location = location;
        this.capacity = capacity;
        this.costPerMinute = costPerMinute;
        this.occupiedSpaces = 0;
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

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Integer getOccupiedSpaces() {
        return occupiedSpaces;
    }

    public void setOccupiedSpaces(Integer occupiedSpaces) {
        this.occupiedSpaces = occupiedSpaces;
    }

    public BigDecimal getCostPerMinute() {
        return costPerMinute;
    }

    public void setCostPerMinute(BigDecimal costPerMinute) {
        this.costPerMinute = costPerMinute;
    }

    public int getAvailableSpaces() {
        return capacity - occupiedSpaces;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ParkingLot)) return false;
        ParkingLot that = (ParkingLot) o;
        return Objects.equals(lotId, that.lotId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lotId);
    }
}
