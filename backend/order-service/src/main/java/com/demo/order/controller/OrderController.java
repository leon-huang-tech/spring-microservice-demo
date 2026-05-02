package com.demo.order.controller;

import com.demo.order.model.Order;
import com.demo.order.service.OrderService;
import com.demo.order.service.UserClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

	private final OrderService orderService;
	private final UserClient userClient;

	public OrderController(OrderService orderService, UserClient userClient) {
		this.orderService = orderService;
		this.userClient = userClient;
	}

	@GetMapping
	public ResponseEntity<List<Order>> getAllOrders() {
		return ResponseEntity.ok(orderService.getAllOrders());
	}

	@GetMapping("/{id}")
	public ResponseEntity<Order> getOrderById(@PathVariable("id") Long id) {
		return orderService.getOrderById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
	}

	@GetMapping("/user/{userId}")
	public ResponseEntity<Map<String, Object>> getOrdersWithUser(@PathVariable("userId") Long userId) {
		List<Order> orders = orderService.getOrdersByUserId(userId);
		Map<String, Object> userInfo = userClient.getUserById(userId);

		Map<String, Object> response = new HashMap<>();
		response.put("user", userInfo);
		response.put("orders", orders);
		response.put("totalOrders", orders.size());

		return ResponseEntity.ok(response);
	}

	@PostMapping
	public ResponseEntity<Order> createOrder(@RequestBody Order order) {
		return ResponseEntity.ok(orderService.createOrder(order));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteOrder(@PathVariable("id") Long id) {
		orderService.deleteOrder(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/health")
	public ResponseEntity<String> health() {
		return ResponseEntity.ok("Order Service is running");
	}
}