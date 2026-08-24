package com.smartpark.service;

import com.smartpark.dto.OccupancyResponse;
import com.smartpark.dto.ParkingLotRequest;
import com.smartpark.dto.ParkingLotResponse;
import com.smartpark.entity.ParkingLot;
import com.smartpark.exception.DuplicateResourceException;
import com.smartpark.exception.ResourceNotFoundException;
import com.smartpark.repository.ParkingLotRepository;
import com.smartpark.repository.ParkingSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParkingLotServiceTest {

    @Mock
    private ParkingLotRepository parkingLotRepository;
    @Mock
    private ParkingSessionRepository parkingSessionRepository;

    private ParkingLotService parkingLotService;

    @BeforeEach
    void setUp() {
        parkingLotService = new ParkingLotService(parkingLotRepository, parkingSessionRepository);
    }

    @Test
    void registerLot_succeeds_whenLotIdNotTaken() {
        ParkingLotRequest request = new ParkingLotRequest();
        request.setLotId("LOT-999");
        request.setLocation("Test Location");
        request.setCapacity(20);
        request.setCostPerMinute(new BigDecimal("0.30"));

        when(parkingLotRepository.existsById("LOT-999")).thenReturn(false);
        when(parkingLotRepository.save(any(ParkingLot.class))).thenAnswer(inv -> inv.getArgument(0));

        ParkingLotResponse response = parkingLotService.registerLot(request);

        assertEquals("LOT-999", response.getLotId());
        assertEquals(20, response.getCapacity());
        assertEquals(0, response.getOccupiedSpaces());
        assertEquals(20, response.getAvailableSpaces());
    }

    @Test
    void registerLot_throws_whenLotIdAlreadyExists() {
        ParkingLotRequest request = new ParkingLotRequest();
        request.setLotId("LOT-001");
        request.setLocation("Downtown");
        request.setCapacity(10);
        request.setCostPerMinute(new BigDecimal("0.50"));

        when(parkingLotRepository.existsById("LOT-001")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> parkingLotService.registerLot(request));
    }

    @Test
    void getOccupancy_returnsCorrectAvailability() {
        ParkingLot lot = new ParkingLot("LOT-001", "Downtown", 10, new BigDecimal("0.50"));
        lot.setOccupiedSpaces(4);

        when(parkingLotRepository.findById("LOT-001")).thenReturn(Optional.of(lot));

        OccupancyResponse response = parkingLotService.getOccupancy("LOT-001");

        assertEquals(10, response.getCapacity());
        assertEquals(4, response.getOccupiedSpaces());
        assertEquals(6, response.getAvailableSpaces());
    }

    @Test
    void getOccupancy_throws_whenLotDoesNotExist() {
        when(parkingLotRepository.findById("MISSING")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> parkingLotService.getOccupancy("MISSING"));
    }
}
