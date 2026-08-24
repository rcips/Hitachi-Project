package com.smartpark.service;

import com.smartpark.dto.OccupancyResponse;
import com.smartpark.dto.ParkedVehicleResponse;
import com.smartpark.dto.ParkingLotRequest;
import com.smartpark.dto.ParkingLotResponse;
import com.smartpark.entity.ParkingLot;
import com.smartpark.entity.ParkingSession;
import com.smartpark.entity.SessionStatus;
import com.smartpark.exception.DuplicateResourceException;
import com.smartpark.exception.ResourceNotFoundException;
import com.smartpark.repository.ParkingLotRepository;
import com.smartpark.repository.ParkingSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ParkingLotService {

    private final ParkingLotRepository parkingLotRepository;
    private final ParkingSessionRepository parkingSessionRepository;

    public ParkingLotService(ParkingLotRepository parkingLotRepository,
                              ParkingSessionRepository parkingSessionRepository) {
        this.parkingLotRepository = parkingLotRepository;
        this.parkingSessionRepository = parkingSessionRepository;
    }

    @Transactional
    public ParkingLotResponse registerLot(ParkingLotRequest request) {
        if (parkingLotRepository.existsById(request.getLotId())) {
            throw new DuplicateResourceException("A parking lot with lotId '" + request.getLotId() + "' already exists");
        }
        ParkingLot lot = new ParkingLot(request.getLotId(), request.getLocation(),
                request.getCapacity(), request.getCostPerMinute());
        lot = parkingLotRepository.save(lot);
        return toLotResponse(lot);
    }

    @Transactional(readOnly = true)
    public List<ParkingLotResponse> listAll() {
        return parkingLotRepository.findAll().stream()
                .map(this::toLotResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OccupancyResponse getOccupancy(String lotId) {
        ParkingLot lot = getLotOrThrow(lotId);
        return new OccupancyResponse(lot.getLotId(), lot.getLocation(), lot.getCapacity(),
                lot.getOccupiedSpaces(), lot.getAvailableSpaces());
    }

    @Transactional(readOnly = true)
    public List<ParkedVehicleResponse> listParkedVehicles(String lotId) {
        // validate the lot exists first, for a clean 404 instead of an empty list
        getLotOrThrow(lotId);

        List<ParkingSession> activeSessions =
                parkingSessionRepository.findByParkingLot_LotIdAndStatus(lotId, SessionStatus.ACTIVE);

        LocalDateTime now = LocalDateTime.now();
        return activeSessions.stream()
                .map(session -> new ParkedVehicleResponse(
                        session.getVehicle().getLicensePlate(),
                        session.getVehicle().getType(),
                        session.getVehicle().getOwnerName(),
                        session.getCheckInTime(),
                        Duration.between(session.getCheckInTime(), now).toMinutes()))
                .collect(Collectors.toList());
    }

    private ParkingLot getLotOrThrow(String lotId) {
        return parkingLotRepository.findById(lotId)
                .orElseThrow(() -> new ResourceNotFoundException("Parking lot not found: " + lotId));
    }

    private ParkingLotResponse toLotResponse(ParkingLot lot) {
        return new ParkingLotResponse(lot.getLotId(), lot.getLocation(), lot.getCapacity(),
                lot.getOccupiedSpaces(), lot.getAvailableSpaces(), lot.getCostPerMinute());
    }
}
