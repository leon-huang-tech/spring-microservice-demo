package com.demo.ai.service;

import com.demo.ai.client.OrderClient;
import com.demo.ai.client.UserClient;
import com.demo.ai.prompt.AiPrompts;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class DataService {

    private final UserClient userClient;
    private final OrderClient orderClient;
    private final ObjectMapper objectMapper;

    public DataService(UserClient userClient,
                       OrderClient orderClient,
                       ObjectMapper objectMapper) {
        this.userClient = userClient;
        this.orderClient = orderClient;
        this.objectMapper = objectMapper;
    }

    @Tool(description = AiPrompts.SYSTEM_TOOLS_GET_USERS)
    public String getAllUsers() {
        try {
            List<Map<String, Object>> users = userClient.getAllUsers();
            return objectMapper.writeValueAsString(users);
        } catch (Exception e) {
            return "Failed to fetch users: " + e.getMessage();
        }
    }

    @Tool(description = AiPrompts.SYSTEM_TOOLS_GET_ORDERS)
    public String getAllOrders() {
        try {
            List<Map<String, Object>> orders = orderClient.getAllOrders();
            return objectMapper.writeValueAsString(orders);
        } catch (Exception e) {
            return "Failed to fetch orders: " + e.getMessage();
        }
    }

    @Tool(description = AiPrompts.SYSTEM_TOOLS_GET_ORDERS_BY_USER)
    public String getOrdersByUserId(Long userId) {
        try {
            Map<String, Object> result = orderClient.getOrdersByUserId(userId);
            return objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            return "Failed to fetch orders for user: " + e.getMessage();
        }
    }

    @Tool(description = AiPrompts.SYSTEM_TOOLS_GET_USER_BY_ID)
    public String getUserById(Long userId) {
        try {
            Map<String, Object> user = userClient.getUserById(userId);
            return objectMapper.writeValueAsString(user);
        } catch (Exception e) {
            return "Failed to fetch user: " + e.getMessage();
        }
    }
}