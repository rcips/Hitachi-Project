package com.smartpark.dto;

import javax.validation.constraints.NotBlank;

public class CheckOutRequest {

    @NotBlank(message = "licensePlate is required")
    private String licensePlate;

    public CheckOutRequest() {
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }
}
