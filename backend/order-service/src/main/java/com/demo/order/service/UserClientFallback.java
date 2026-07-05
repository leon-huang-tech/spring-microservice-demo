package com.demo.order.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

// Resilience4j fallback - if user-service is down, return default response
@Component
public class UserClientFallback implements UserClient {

    @Override
    public Map<String, Object> getUserById(Long id) {
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("id", id);
        fallback.put("name", "Unknown User");
        fallback.put("email", "N/A");
        fallback.put("fallback", true);
        return fallback;
    }
}
