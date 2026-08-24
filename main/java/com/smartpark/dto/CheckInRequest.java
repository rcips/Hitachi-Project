package com.smartpark.dto;

import javax.validation.constraints.NotBlank;

public class CheckInRequest {

    @NotBlank(message = "lotId is required")
    private String lotId;

    @NotBlank(message = "licensePlate is required")
    private String licensePlate;

    public CheckInRequest() {
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
}
