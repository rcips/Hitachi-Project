package com.smartpark.controller;

import com.smartpark.dto.CheckInRequest;
import com.smartpark.dto.CheckInResponse;
import com.smartpark.dto.CheckOutRequest;
import com.smartpark.dto.CheckOutResponse;
import com.smartpark.service.ParkingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/parking")
public class ParkingController {

    private final ParkingService parkingService;

    public ParkingController(ParkingService parkingService) {
        this.parkingService = parkingService;
    }

    /** Checks a registered vehicle into a parking lot. Fails if the lot is full
     *  or the vehicle is already parked somewhere else. */
    @PostMapping("/check-in")
    public ResponseEntity<CheckInResponse> checkIn(@Valid @RequestBody CheckInRequest request) {
        return ResponseEntity.ok(parkingService.checkIn(request));
    }

    /** Checks a vehicle out of its current lot and returns the computed parking cost. */
    @PostMapping("/check-out")
    public ResponseEntity<CheckOutResponse> checkOut(@Valid @RequestBody CheckOutRequest request) {
        return ResponseEntity.ok(parkingService.checkOut(request));
    }
}
