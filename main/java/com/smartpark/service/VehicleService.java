package com.smartpark.service;

import com.smartpark.dto.VehicleRequest;
import com.smartpark.dto.VehicleResponse;
import com.smartpark.entity.Vehicle;
import com.smartpark.exception.DuplicateResourceException;
import com.smartpark.exception.ResourceNotFoundException;
import com.smartpark.repository.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    public VehicleService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    @Transactional
    public VehicleResponse registerVehicle(VehicleRequest request) {
        if (vehicleRepository.existsById(request.getLicensePlate())) {
            throw new DuplicateResourceException(
                    "A vehicle with license plate '" + request.getLicensePlate() + "' is already registered");
        }
        Vehicle vehicle = new Vehicle(request.getLicensePlate(), request.getType(), request.getOwnerName());
        vehicle = vehicleRepository.save(vehicle);
        return toResponse(vehicle);
    }

    @Transactional(readOnly = true)
    public List<VehicleResponse> listAll() {
        return vehicleRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public VehicleResponse getByLicensePlate(String licensePlate) {
        Vehicle vehicle = vehicleRepository.findById(licensePlate)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + licensePlate));
        return toResponse(vehicle);
    }

    private VehicleResponse toResponse(Vehicle vehicle) {
        return new VehicleResponse(vehicle.getLicensePlate(), vehicle.getType(), vehicle.getOwnerName());
    }
}
