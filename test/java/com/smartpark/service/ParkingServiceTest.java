package com.smartpark.service;

import com.smartpark.dto.CheckInRequest;
import com.smartpark.dto.CheckInResponse;
import com.smartpark.dto.CheckOutRequest;
import com.smartpark.dto.CheckOutResponse;
import com.smartpark.entity.ParkingLot;
import com.smartpark.entity.ParkingSession;
import com.smartpark.entity.SessionStatus;
import com.smartpark.entity.Vehicle;
import com.smartpark.entity.VehicleType;
import com.smartpark.exception.ParkingLotFullException;
import com.smartpark.exception.ResourceNotFoundException;
import com.smartpark.exception.VehicleAlreadyParkedException;
import com.smartpark.exception.VehicleNotParkedException;
import com.smartpark.repository.ParkingLotRepository;
import com.smartpark.repository.ParkingSessionRepository;
import com.smartpark.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParkingServiceTest {

    @Mock
    private ParkingLotRepository parkingLotRepository;
    @Mock
    private VehicleRepository vehicleRepository;
    @Mock
    private ParkingSessionRepository parkingSessionRepository;

    private ParkingService parkingService;

    private Vehicle vehicle;
    private ParkingLot lot;

    @BeforeEach
    void setUp() {
        parkingService = new ParkingService(parkingLotRepository, vehicleRepository, parkingSessionRepository);
        vehicle = new Vehicle("ABC-123", VehicleType.CAR, "John Doe");
        lot = new ParkingLot("LOT-001", "Downtown Plaza", 2, new BigDecimal("0.50"));
    }

    @Test
    void checkIn_succeeds_whenLotHasSpaceAndVehicleFree() {
        CheckInRequest request = new CheckInRequest();
        request.setLotId("LOT-001");
        request.setLicensePlate("ABC-123");

        when(vehicleRepository.findById("ABC-123")).thenReturn(Optional.of(vehicle));
        when(parkingSessionRepository.findByVehicle_LicensePlateAndStatus("ABC-123", SessionStatus.ACTIVE))
                .thenReturn(Optional.empty());
        when(parkingLotRepository.findByIdForUpdate("LOT-001")).thenReturn(Optional.of(lot));
        when(parkingSessionRepository.save(any(ParkingSession.class))).thenAnswer(invocation -> {
            ParkingSession s = invocation.getArgument(0);
            s.setId(1L);
            return s;
        });

        CheckInResponse response = parkingService.checkIn(request);

        assertEquals("LOT-001", response.getLotId());
        assertEquals("ABC-123", response.getLicensePlate());
        assertEquals(1, lot.getOccupiedSpaces()); // incremented
        verify(parkingLotRepository, times(1)).save(lot);
    }

    @Test
    void checkIn_throws_whenLotIsFull() {
        lot.setOccupiedSpaces(2); // capacity is 2 -> full

        CheckInRequest request = new CheckInRequest();
        request.setLotId("LOT-001");
        request.setLicensePlate("ABC-123");

        when(vehicleRepository.findById("ABC-123")).thenReturn(Optional.of(vehicle));
        when(parkingSessionRepository.findByVehicle_LicensePlateAndStatus("ABC-123", SessionStatus.ACTIVE))
                .thenReturn(Optional.empty());
        when(parkingLotRepository.findByIdForUpdate("LOT-001")).thenReturn(Optional.of(lot));

        assertThrows(ParkingLotFullException.class, () -> parkingService.checkIn(request));
        verify(parkingSessionRepository, times(0)).save(any());
    }

    @Test
    void checkIn_throws_whenVehicleAlreadyParkedElsewhere() {
        CheckInRequest request = new CheckInRequest();
        request.setLotId("LOT-001");
        request.setLicensePlate("ABC-123");

        ParkingLot otherLot = new ParkingLot("LOT-002", "Airport", 10, new BigDecimal("0.75"));
        ParkingSession activeSession = new ParkingSession();
        activeSession.setParkingLot(otherLot);

        when(vehicleRepository.findById("ABC-123")).thenReturn(Optional.of(vehicle));
        when(parkingSessionRepository.findByVehicle_LicensePlateAndStatus("ABC-123", SessionStatus.ACTIVE))
                .thenReturn(Optional.of(activeSession));

        assertThrows(VehicleAlreadyParkedException.class, () -> parkingService.checkIn(request));
    }

    @Test
    void checkIn_throws_whenVehicleNotRegistered() {
        CheckInRequest request = new CheckInRequest();
        request.setLotId("LOT-001");
        request.setLicensePlate("UNKNOWN-1");

        when(vehicleRepository.findById("UNKNOWN-1")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> parkingService.checkIn(request));
    }

    @Test
    void checkOut_calculatesCost_andFreesSpace() {
        lot.setOccupiedSpaces(1);

        ParkingSession session = new ParkingSession();
        session.setId(10L);
        session.setVehicle(vehicle);
        session.setParkingLot(lot);
        session.setCheckInTime(LocalDateTime.now().minusMinutes(10));
        session.setStatus(SessionStatus.ACTIVE);

        CheckOutRequest request = new CheckOutRequest();
        request.setLicensePlate("ABC-123");

        when(parkingSessionRepository.findByVehicle_LicensePlateAndStatus("ABC-123", SessionStatus.ACTIVE))
                .thenReturn(Optional.of(session));
        when(parkingLotRepository.findByIdForUpdate("LOT-001")).thenReturn(Optional.of(lot));

        CheckOutResponse response = parkingService.checkOut(request);

        assertEquals("ABC-123", response.getLicensePlate());
        assertEquals(0, lot.getOccupiedSpaces()); // decremented back to 0
        // ~10 minutes parked at 0.50/min => ~5.00, allow rounding to at least 10 minutes worth
        assertEquals(new BigDecimal("0.50").multiply(BigDecimal.valueOf(response.getMinutesParked())).setScale(2),
                response.getCost());

        ArgumentCaptor<ParkingSession> captor = ArgumentCaptor.forClass(ParkingSession.class);
        verify(parkingSessionRepository, times(1)).save(captor.capture());
        assertEquals(SessionStatus.COMPLETED, captor.getValue().getStatus());
    }

    @Test
    void checkOut_throws_whenVehicleNotCurrentlyParked() {
        CheckOutRequest request = new CheckOutRequest();
        request.setLicensePlate("ABC-123");

        when(parkingSessionRepository.findByVehicle_LicensePlateAndStatus("ABC-123", SessionStatus.ACTIVE))
                .thenReturn(Optional.empty());

        assertThrows(VehicleNotParkedException.class, () -> parkingService.checkOut(request));
    }

    @Test
    void calculateBillableMinutes_roundsUp_withOneMinuteMinimum() {
        LocalDateTime start = LocalDateTime.of(2026, 1, 1, 10, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 1, 1, 10, 0, 30); // 30 seconds
        assertEquals(1, ParkingService.calculateBillableMinutes(start, end));

        LocalDateTime end2 = LocalDateTime.of(2026, 1, 1, 10, 15, 1); // 15 min 1 sec
        assertEquals(16, ParkingService.calculateBillableMinutes(start, end2));
    }
}
