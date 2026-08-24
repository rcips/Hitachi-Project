package com.smartpark.service;

import com.smartpark.dto.CheckInRequest;
import com.smartpark.dto.CheckInResponse;
import com.smartpark.dto.CheckOutRequest;
import com.smartpark.dto.CheckOutResponse;
import com.smartpark.entity.ParkingLot;
import com.smartpark.entity.ParkingSession;
import com.smartpark.entity.SessionStatus;
import com.smartpark.entity.Vehicle;
import com.smartpark.exception.ParkingLotFullException;
import com.smartpark.exception.ResourceNotFoundException;
import com.smartpark.exception.VehicleAlreadyParkedException;
import com.smartpark.exception.VehicleNotParkedException;
import com.smartpark.repository.ParkingLotRepository;
import com.smartpark.repository.ParkingSessionRepository;
import com.smartpark.repository.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Handles the check-in / check-out lifecycle of a vehicle in a parking lot,
 * including capacity enforcement and cost calculation.
 */
@Service
public class ParkingService {

    private final ParkingLotRepository parkingLotRepository;
    private final VehicleRepository vehicleRepository;
    private final ParkingSessionRepository parkingSessionRepository;

    public ParkingService(ParkingLotRepository parkingLotRepository,
                           VehicleRepository vehicleRepository,
                           ParkingSessionRepository parkingSessionRepository) {
        this.parkingLotRepository = parkingLotRepository;
        this.vehicleRepository = vehicleRepository;
        this.parkingSessionRepository = parkingSessionRepository;
    }

    @Transactional
    public CheckInResponse checkIn(CheckInRequest request) {
        Vehicle vehicle = vehicleRepository.findById(request.getLicensePlate())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Vehicle is not registered: " + request.getLicensePlate() + ". Register it first via POST /api/vehicles"));

        // A vehicle can only be parked in one lot at a time.
        parkingSessionRepository.findByVehicle_LicensePlateAndStatus(request.getLicensePlate(), SessionStatus.ACTIVE)
                .ifPresent(existing -> {
                    throw new VehicleAlreadyParkedException(
                            "Vehicle " + request.getLicensePlate() + " is already checked in at lot "
                                    + existing.getParkingLot().getLotId());
                });

        // Pessimistic lock on the lot row to make check-in safe under concurrent requests.
        ParkingLot lot = parkingLotRepository.findByIdForUpdate(request.getLotId())
                .orElseThrow(() -> new ResourceNotFoundException("Parking lot not found: " + request.getLotId()));

        if (lot.getOccupiedSpaces() >= lot.getCapacity()) {
            throw new ParkingLotFullException("Parking lot " + lot.getLotId() + " is full (capacity " + lot.getCapacity() + ")");
        }

        lot.setOccupiedSpaces(lot.getOccupiedSpaces() + 1);
        parkingLotRepository.save(lot);

        ParkingSession session = new ParkingSession();
        session.setVehicle(vehicle);
        session.setParkingLot(lot);
        session.setCheckInTime(LocalDateTime.now());
        session.setStatus(SessionStatus.ACTIVE);
        session = parkingSessionRepository.save(session);

        return new CheckInResponse(session.getId(), lot.getLotId(), vehicle.getLicensePlate(),
                session.getCheckInTime(), lot.getAvailableSpaces());
    }

    @Transactional
    public CheckOutResponse checkOut(CheckOutRequest request) {
        ParkingSession session = parkingSessionRepository
                .findByVehicle_LicensePlateAndStatus(request.getLicensePlate(), SessionStatus.ACTIVE)
                .orElseThrow(() -> new VehicleNotParkedException(
                        "Vehicle " + request.getLicensePlate() + " does not currently have an active parking session"));

        // Re-fetch the lot with a write lock to safely decrement occupancy.
        ParkingLot lot = parkingLotRepository.findByIdForUpdate(session.getParkingLot().getLotId())
                .orElseThrow(() -> new ResourceNotFoundException("Parking lot not found: " + session.getParkingLot().getLotId()));

        LocalDateTime now = LocalDateTime.now();
        long minutesParked = calculateBillableMinutes(session.getCheckInTime(), now);
        BigDecimal cost = calculateCost(lot.getCostPerMinute(), minutesParked);

        session.setCheckOutTime(now);
        session.setCost(cost);
        session.setStatus(SessionStatus.COMPLETED);
        parkingSessionRepository.save(session);

        lot.setOccupiedSpaces(Math.max(0, lot.getOccupiedSpaces() - 1));
        parkingLotRepository.save(lot);

        return new CheckOutResponse(session.getId(), lot.getLotId(), session.getVehicle().getLicensePlate(),
                session.getCheckInTime(), session.getCheckOutTime(), minutesParked, cost);
    }

    /**
     * Billable minutes are rounded up and a minimum of 1 minute is charged,
     * so that even very short stays are billed fairly.
     */
    static long calculateBillableMinutes(LocalDateTime checkIn, LocalDateTime checkOut) {
        long seconds = Duration.between(checkIn, checkOut).getSeconds();
        long minutes = (long) Math.ceil(seconds / 60.0);
        return Math.max(1, minutes);
    }

    static BigDecimal calculateCost(BigDecimal costPerMinute, long minutesParked) {
        return costPerMinute.multiply(BigDecimal.valueOf(minutesParked)).setScale(2, RoundingMode.HALF_UP);
    }
}
