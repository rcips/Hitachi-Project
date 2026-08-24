package com.smartpark.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartpark.dto.CheckInRequest;
import com.smartpark.dto.CheckOutRequest;
import com.smartpark.dto.LoginRequest;
import com.smartpark.dto.ParkingLotRequest;
import com.smartpark.dto.VehicleRequest;
import com.smartpark.entity.VehicleType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end test exercising the real HTTP layer (security filter chain included):
 * login -> register lot -> register vehicle -> check-in -> occupancy -> check-out.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SmartParkApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void protectedEndpoint_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/parking-lots"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_withBadCredentials_returns401() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("wrong-password");

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void fullHappyPath_login_registerLot_registerVehicle_checkIn_checkOut() throws Exception {
        String token = login();

        // Register a lot
        ParkingLotRequest lotRequest = new ParkingLotRequest();
        lotRequest.setLotId("IT-LOT-001");
        lotRequest.setLocation("Integration Test Lot");
        lotRequest.setCapacity(1);
        lotRequest.setCostPerMinute(new BigDecimal("1.00"));

        mockMvc.perform(post("/api/parking-lots")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(lotRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.lotId").value("IT-LOT-001"))
                .andExpect(jsonPath("$.availableSpaces").value(1));

        // Register a vehicle
        VehicleRequest vehicleRequest = new VehicleRequest();
        vehicleRequest.setLicensePlate("IT-999");
        vehicleRequest.setType(VehicleType.CAR);
        vehicleRequest.setOwnerName("Test Owner");

        mockMvc.perform(post("/api/vehicles")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(vehicleRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.licensePlate").value("IT-999"));

        // Check in
        CheckInRequest checkInRequest = new CheckInRequest();
        checkInRequest.setLotId("IT-LOT-001");
        checkInRequest.setLicensePlate("IT-999");

        mockMvc.perform(post("/api/parking/check-in")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(checkInRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lotId").value("IT-LOT-001"))
                .andExpect(jsonPath("$.availableSpacesRemaining").value(0));

        // Lot is now full: a second check-in should fail
        VehicleRequest vehicleRequest2 = new VehicleRequest();
        vehicleRequest2.setLicensePlate("IT-998");
        vehicleRequest2.setType(VehicleType.CAR);
        vehicleRequest2.setOwnerName("Second Owner");
        mockMvc.perform(post("/api/vehicles")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(vehicleRequest2)))
                .andExpect(status().isCreated());

        CheckInRequest secondCheckIn = new CheckInRequest();
        secondCheckIn.setLotId("IT-LOT-001");
        secondCheckIn.setLicensePlate("IT-998");
        mockMvc.perform(post("/api/parking/check-in")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(secondCheckIn)))
                .andExpect(status().isConflict());

        // Occupancy reflects the checked-in vehicle
        mockMvc.perform(get("/api/parking-lots/IT-LOT-001/occupancy")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.occupiedSpaces").value(1))
                .andExpect(jsonPath("$.availableSpaces").value(0));

        // Vehicle appears in the parked-vehicles listing
        mockMvc.perform(get("/api/parking-lots/IT-LOT-001/vehicles")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].licensePlate").value("IT-999"));

        // Check out and verify a cost was computed
        CheckOutRequest checkOutRequest = new CheckOutRequest();
        checkOutRequest.setLicensePlate("IT-999");

        mockMvc.perform(post("/api/parking/check-out")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(checkOutRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.licensePlate").value("IT-999"))
                .andExpect(jsonPath("$.cost").exists());

        // Space is freed
        mockMvc.perform(get("/api/parking-lots/IT-LOT-001/occupancy")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.occupiedSpaces").value(0))
                .andExpect(jsonPath("$.availableSpaces").value(1));
    }

    private String login() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("Admin@123");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        return objectMapper.readTree(body).get("token").asText();
    }
}
