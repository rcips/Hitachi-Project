package com.smartpark.controller;

import com.smartpark.dto.VehicleRequest;
import com.smartpark.dto.VehicleResponse;
import com.smartpark.service.VehicleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    /** Registers a new vehicle. */
    @PostMapping
    public ResponseEntity<VehicleResponse> register(@Valid @RequestBody VehicleRequest request) {
        VehicleResponse response = vehicleService.registerVehicle(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /** Lists all registered vehicles. */
    @GetMapping
    public ResponseEntity<List<VehicleResponse>> listAll() {
        return ResponseEntity.ok(vehicleService.listAll());
    }

    /** Fetches a single vehicle by license plate. */
    @GetMapping("/{licensePlate}")
    public ResponseEntity<VehicleResponse> getByLicensePlate(@PathVariable String licensePlate) {
        return ResponseEntity.ok(vehicleService.getByLicensePlate(licensePlate));
    }
}
