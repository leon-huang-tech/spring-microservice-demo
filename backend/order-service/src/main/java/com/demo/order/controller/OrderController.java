package com.demo.order.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.demo.order.dto.ApiResponse;
import com.demo.order.model.Order;
import com.demo.order.service.OrderService;
import com.demo.order.service.UserClient;

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
//		return orderService.getOrderById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
		return ResponseEntity.ok(orderService.getOrderById(id));
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

	@GetMapping("/paged")
	public ResponseEntity<ApiResponse<Page<Order>>> getOrdersPaged(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "5") int size) {
		return ResponseEntity.ok(ApiResponse.success(orderService.getOrdersPaged(page, size)));
	}
	
	@PutMapping("/{id}")
	public ResponseEntity<Order> updateOrder(
	        @PathVariable("id") Long id,
	        @RequestBody Order order) {
//	    return orderService.getOrderById(id) instanceof Order existing
//	        ? ResponseEntity.ok(orderService.updateOrder(id, order))
//	        : ResponseEntity.notFound().build();
		return ResponseEntity.ok(orderService.updateOrder(id, order));
	}
}