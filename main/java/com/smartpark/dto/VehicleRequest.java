package com.smartpark.dto;

import com.smartpark.entity.VehicleType;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

public class VehicleRequest {

    @NotBlank(message = "licensePlate is required")
    @Pattern(regexp = "^[A-Za-z0-9-]+$", message = "licensePlate may only contain letters, numbers and dashes")
    private String licensePlate;

    @NotNull(message = "type is required (CAR, MOTORCYCLE or TRUCK)")
    private VehicleType type;

    @NotBlank(message = "ownerName is required")
    @Pattern(regexp = "^[A-Za-z ]+$", message = "ownerName may only contain letters and spaces")
    private String ownerName;

    public VehicleRequest() {
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
}
