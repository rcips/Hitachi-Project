package com.smartpark.repository;

import com.smartpark.entity.ParkingLot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.persistence.LockModeType;
import java.util.Optional;

public interface ParkingLotRepository extends JpaRepository<ParkingLot, String> {

    /**
     * Fetches a parking lot with a pessimistic write lock so that concurrent
     * check-in / check-out requests for the same lot cannot race each other
     * and over/under-count occupied spaces.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from ParkingLot p where p.lotId = :lotId")
    Optional<ParkingLot> findByIdForUpdate(@Param("lotId") String lotId);
}
