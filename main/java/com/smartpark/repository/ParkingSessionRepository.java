package com.smartpark.repository;

import com.smartpark.entity.ParkingSession;
import com.smartpark.entity.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ParkingSessionRepository extends JpaRepository<ParkingSession, Long> {

    /** Finds the currently active session (if any) for a given vehicle. Used to enforce
     *  the "a vehicle can only be parked in one lot at a time" rule and to look up
     *  the vehicle's current lot on check-out. */
    Optional<ParkingSession> findByVehicle_LicensePlateAndStatus(String licensePlate, SessionStatus status);

    /** Lists all sessions with the given status for a given lot (e.g. all ACTIVE
     *  sessions = vehicles currently parked in that lot). */
    List<ParkingSession> findByParkingLot_LotIdAndStatus(String lotId, SessionStatus status);

    /** Used by the scheduler to find vehicles that have overstayed the allowed duration. */
    List<ParkingSession> findByStatusAndCheckInTimeBefore(SessionStatus status, LocalDateTime threshold);
}
