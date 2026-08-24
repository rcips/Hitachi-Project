package com.smartpark.controller;

import com.smartpark.dto.OccupancyResponse;
import com.smartpark.dto.ParkedVehicleResponse;
import com.smartpark.dto.ParkingLotRequest;
import com.smartpark.dto.ParkingLotResponse;
import com.smartpark.service.ParkingLotService;
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
@RequestMapping("/api/parking-lots")
public class ParkingLotController {

    private final ParkingLotService parkingLotService;

    public ParkingLotController(ParkingLotService parkingLotService) {
        this.parkingLotService = parkingLotService;
    }

    /** Registers a new parking lot. */
    @PostMapping
    public ResponseEntity<ParkingLotResponse> register(@Valid @RequestBody ParkingLotRequest request) {
        ParkingLotResponse response = parkingLotService.registerLot(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /** Lists all registered parking lots. */
    @GetMapping
    public ResponseEntity<List<ParkingLotResponse>> listAll() {
        return ResponseEntity.ok(parkingLotService.listAll());
    }

    /** Returns current occupancy and availability for a specific lot. */
    @GetMapping("/{lotId}/occupancy")
    public ResponseEntity<OccupancyResponse> getOccupancy(@PathVariable String lotId) {
        return ResponseEntity.ok(parkingLotService.getOccupancy(lotId));
    }

    /** Lists all vehicles currently parked in the given lot. */
    @GetMapping("/{lotId}/vehicles")
    public ResponseEntity<List<ParkedVehicleResponse>> listParkedVehicles(@PathVariable String lotId) {
        return ResponseEntity.ok(parkingLotService.listParkedVehicles(lotId));
    }
}
