package com.smartpark.service;

import com.smartpark.config.JwtUtil;
import com.smartpark.dto.LoginResponse;
import com.smartpark.exception.InvalidCredentialsException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Authenticates callers against a single, statically configured
 * username/password (see smartpark.auth.* in application.yml) and issues
 * a JWT token on success, as required by the assignment spec.
 */
@Service
public class AuthService {

    private final String configuredUsername;
    private final String configuredPassword;
    private final JwtUtil jwtUtil;

    public AuthService(@Value("${smartpark.auth.username}") String configuredUsername,
                        @Value("${smartpark.auth.password}") String configuredPassword,
                        JwtUtil jwtUtil) {
        this.configuredUsername = configuredUsername;
        this.configuredPassword = configuredPassword;
        this.jwtUtil = jwtUtil;
    }

    public LoginResponse login(String username, String password) {
        if (username == null || password == null
                || !configuredUsername.equals(username) || !configuredPassword.equals(password)) {
            throw new InvalidCredentialsException("Invalid username or password");
        }
        String token = jwtUtil.generateToken(username);
        return new LoginResponse(token, jwtUtil.getExpirationMs());
    }
}
