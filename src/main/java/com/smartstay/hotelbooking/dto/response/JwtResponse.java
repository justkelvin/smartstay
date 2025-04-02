package com.smartstay.hotelbooking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JwtResponse {

    private Long userId;
    private String username;
    private String email;
    private String role;
    private String token;
    private int expiresIn;
}