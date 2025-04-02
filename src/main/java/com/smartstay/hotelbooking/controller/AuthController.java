package com.smartstay.hotelbooking.controller;

import com.smartstay.hotelbooking.dto.request.LoginRequest;
import com.smartstay.hotelbooking.dto.request.RegisterRequest;
import com.smartstay.hotelbooking.dto.response.JwtResponse;
import com.smartstay.hotelbooking.model.entity.User;
import com.smartstay.hotelbooking.security.JwtTokenProvider;
import com.smartstay.hotelbooking.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;

    @Value("${app.jwt.expiration-ms}")
    private int jwtExpirationMs;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager,
            JwtTokenProvider jwtTokenProvider,
            UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtTokenProvider.generateToken(authentication);

        // Get user details
        User user = userService.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found with username: " + loginRequest.getUsername()));

        // Update last login time
        user.setLastLogin(LocalDateTime.now());
        userService.updateUser(user);

        JwtResponse response = new JwtResponse();
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole().name());
        response.setToken(jwt);
        response.setExpiresIn(jwtExpirationMs / 1000); // Convert to seconds

        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        // Check if username is already taken
        if (userService.existsByUsername(registerRequest.getUsername())) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Username is already taken");
            return ResponseEntity.badRequest().body(response);
        }

        // Check if email is already in use
        if (userService.existsByEmail(registerRequest.getEmail())) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Email is already in use");
            return ResponseEntity.badRequest().body(response);
        }

        // Create new user
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(registerRequest.getPassword()); // Will be encoded in the service
        user.setEmail(registerRequest.getEmail());
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setPhoneNumber(registerRequest.getPhoneNumber());
        user.setAddress(registerRequest.getAddress());
        user.setRole(User.UserRole.CUSTOMER); // Default role for new registrations

        User savedUser = userService.registerUser(user);

        Map<String, Object> response = new HashMap<>();
        response.put("userId", savedUser.getId());
        response.put("username", savedUser.getUsername());
        response.put("email", savedUser.getEmail());
        response.put("firstName", savedUser.getFirstName());
        response.put("lastName", savedUser.getLastName());
        response.put("role", savedUser.getRole().name());
        response.put("createdAt", savedUser.getCreatedAt());

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}