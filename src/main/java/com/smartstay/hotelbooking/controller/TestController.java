package com.smartstay.hotelbooking.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class TestController {

    @GetMapping("/api/test")
    public Map<String, String> test() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "API is working!");
        return response;
    }
}