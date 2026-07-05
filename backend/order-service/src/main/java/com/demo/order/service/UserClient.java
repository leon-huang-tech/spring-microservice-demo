package com.demo.order.service;

import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// Feign client: order-service calls user-service via Eureka
@FeignClient(name = "user-service", fallback = UserClientFallback.class)
public interface UserClient {

    @GetMapping("/api/users/{id}")
	Map<String, Object> getUserById(@PathVariable(/* "id" */) Long id);
}
