package com.smartstay.hotelbooking.controller;

import com.smartstay.hotelbooking.model.entity.User;
import com.smartstay.hotelbooking.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getCurrentUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        User user = userService.findByUsername(currentUsername)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Create response with user details but exclude sensitive information
        Map<String, Object> response = new HashMap<>();
        response.put("userId", user.getId());
        response.put("username", user.getUsername());
        response.put("email", user.getEmail());
        response.put("firstName", user.getFirstName());
        response.put("lastName", user.getLastName());
        response.put("phoneNumber", user.getPhoneNumber());
        response.put("address", user.getAddress());
        response.put("role", user.getRole().name());
        response.put("createdAt", user.getCreatedAt());
        response.put("lastLogin", user.getLastLogin());

        return ResponseEntity.ok(response);
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateUserProfile(@RequestBody Map<String, Object> updates, Principal principal) {
        User user = userService.findByUsername(principal.getName())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Update fields if they are present in the request
        if (updates.containsKey("email")) {
            user.setEmail((String) updates.get("email"));
        }

        if (updates.containsKey("firstName")) {
            user.setFirstName((String) updates.get("firstName"));
        }

        if (updates.containsKey("lastName")) {
            user.setLastName((String) updates.get("lastName"));
        }

        if (updates.containsKey("phoneNumber")) {
            user.setPhoneNumber((String) updates.get("phoneNumber"));
        }

        if (updates.containsKey("address")) {
            user.setAddress((String) updates.get("address"));
        }

        User updatedUser = userService.updateUser(user);

        // Create response with updated user details
        Map<String, Object> response = new HashMap<>();
        response.put("userId", updatedUser.getId());
        response.put("username", updatedUser.getUsername());
        response.put("email", updatedUser.getEmail());
        response.put("firstName", updatedUser.getFirstName());
        response.put("lastName", updatedUser.getLastName());
        response.put("phoneNumber", updatedUser.getPhoneNumber());
        response.put("address", updatedUser.getAddress());
        response.put("role", updatedUser.getRole().name());
        response.put("updatedAt", updatedUser.getUpdatedAt());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        User user = userService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));

        // Create response with user details
        Map<String, Object> response = new HashMap<>();
        response.put("userId", user.getId());
        response.put("username", user.getUsername());
        response.put("email", user.getEmail());
        response.put("firstName", user.getFirstName());
        response.put("lastName", user.getLastName());
        response.put("phoneNumber", user.getPhoneNumber());
        response.put("address", user.getAddress());
        response.put("role", user.getRole().name());
        response.put("createdAt", user.getCreatedAt());
        response.put("lastLogin", user.getLastLogin());

        return ResponseEntity.ok(response);
    }
}