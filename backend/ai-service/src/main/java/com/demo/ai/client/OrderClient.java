package com.demo.ai.client;

import java.util.List;
import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "order-service")
public interface OrderClient {

    @GetMapping("/api/orders")
    List<Map<String, Object>> getAllOrders();

    @GetMapping("/api/orders/{id}")
	Map<String, Object> getOrderById(@PathVariable("id") Long id);

    @GetMapping("/api/orders/user/{userId}")
	Map<String, Object> getOrdersByUserId(@PathVariable("userId") Long userId);
}