package com.smartpark.dto;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

public class ParkingLotRequest {

    @NotBlank(message = "lotId is required")
    @Size(max = 50, message = "lotId must not exceed 50 characters")
    private String lotId;

    @NotBlank(message = "location is required")
    private String location;

    @NotNull(message = "capacity is required")
    @Min(value = 1, message = "capacity must be at least 1")
    private Integer capacity;

    @NotNull(message = "costPerMinute is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "costPerMinute must be greater than 0")
    private BigDecimal costPerMinute;

    public ParkingLotRequest() {
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

    public BigDecimal getCostPerMinute() {
        return costPerMinute;
    }

    public void setCostPerMinute(BigDecimal costPerMinute) {
        this.costPerMinute = costPerMinute;
    }
}
