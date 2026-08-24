package com.smartpark.service;

import com.smartpark.entity.ParkingLot;
import com.smartpark.entity.ParkingSession;
import com.smartpark.entity.SessionStatus;
import com.smartpark.repository.ParkingLotRepository;
import com.smartpark.repository.ParkingSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Background job satisfying the requirement: "Vehicles which parked more than
 * 15 minutes should be removed from a parking lot automatically."
 *
 * Runs periodically, finds every ACTIVE session whose check-in time is older
 * than the configured threshold, closes it out (computing the cost the same
 * way a normal checkout would), marks it AUTO_REMOVED, and frees the space.
 */
@Component
public class AutoCheckoutScheduler {

    private static final Logger log = LoggerFactory.getLogger(AutoCheckoutScheduler.class);

    private final ParkingSessionRepository parkingSessionRepository;
    private final ParkingLotRepository parkingLotRepository;
    private final int autoRemovalMinutes;

    public AutoCheckoutScheduler(ParkingSessionRepository parkingSessionRepository,
                                  ParkingLotRepository parkingLotRepository,
                                  @Value("${smartpark.parking.auto-removal-minutes:15}") int autoRemovalMinutes) {
        this.parkingSessionRepository = parkingSessionRepository;
        this.parkingLotRepository = parkingLotRepository;
        this.autoRemovalMinutes = autoRemovalMinutes;
    }

    @Scheduled(fixedRateString = "${smartpark.parking.scheduler-fixed-rate-ms:30000}")
    @Transactional
    public void autoRemoveOverstayedVehicles() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(autoRemovalMinutes);

        List<ParkingSession> overstayed =
                parkingSessionRepository.findByStatusAndCheckInTimeBefore(SessionStatus.ACTIVE, threshold);

        for (ParkingSession session : overstayed) {
            Optional<ParkingLot> lotOpt = parkingLotRepository.findByIdForUpdate(session.getParkingLot().getLotId());
            if (!lotOpt.isPresent()) {
                // Shouldn't normally happen; skip defensively.
                continue;
            }
            ParkingLot lot = lotOpt.get();

            LocalDateTime now = LocalDateTime.now();
            long minutesParked = ParkingService.calculateBillableMinutes(session.getCheckInTime(), now);
            BigDecimal cost = ParkingService.calculateCost(lot.getCostPerMinute(), minutesParked);

            session.setCheckOutTime(now);
            session.setCost(cost);
            session.setStatus(SessionStatus.AUTO_REMOVED);
            parkingSessionRepository.save(session);

            lot.setOccupiedSpaces(Math.max(0, lot.getOccupiedSpaces() - 1));
            parkingLotRepository.save(lot);

            log.info("Auto-removed vehicle {} from lot {} after {} minutes (cost={})",
                    session.getVehicle().getLicensePlate(), lot.getLotId(), minutesParked, cost);
        }
    }
}
