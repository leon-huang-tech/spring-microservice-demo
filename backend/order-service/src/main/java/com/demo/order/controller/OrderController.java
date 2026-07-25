package com.demo.order.controller;

import com.demo.order.dto.ApiResponse;
import com.demo.order.model.Order;
import com.demo.order.service.OrderService;
import com.demo.order.service.UserClient;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
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
	public ResponseEntity<Order> getOrderById(@PathVariable(/* "id" */) @NonNull Long id) {
//		return orderService.getOrderById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
		return ResponseEntity.ok(orderService.getOrderById(id));
	}

	@GetMapping("/user/{userId}")
	public ResponseEntity<Map<String, Object>> getOrdersWithUser(@PathVariable(/* "userId" */) Long userId) {
		List<Order> orders = orderService.getOrdersByUserId(userId);
		Map<String, Object> userInfo = userClient.getUserById(userId);

		Map<String, Object> response = new HashMap<>();
		response.put("user", userInfo);
		response.put("orders", orders);
		response.put("totalOrders", orders.size());

		return ResponseEntity.ok(response);
	}

	@PostMapping
	public ResponseEntity<Order> createOrder(@RequestBody @NonNull Order order) {
		return ResponseEntity.ok(orderService.createOrder(order));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteOrder(@PathVariable(/* "id" */) @NonNull Long id) {
		orderService.deleteOrder(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/health")
	public ResponseEntity<String> health() {
		return ResponseEntity.ok("Order Service is running");
	}

	/**
	 * curl 'http://localhost:8080/api/orders/paged?page=0&size=5' \
	 *   -H "Authorization: Bearer <xxtoken>"
	 */
	@GetMapping("/paged")
	public ResponseEntity<ApiResponse<Page<Order>>> getOrdersPaged(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "5") int size) {
		return ResponseEntity.ok(ApiResponse.success(orderService.getOrdersPaged(page, size)));
	}
	
	@PutMapping("/{id}")
	public ResponseEntity<Order> updateOrder(
			@PathVariable(/* "id" */) Long id,
	        @RequestBody Order order) {
		return ResponseEntity.ok(orderService.updateOrder(id, order));
	}
}