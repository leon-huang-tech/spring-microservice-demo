package com.demo.user.controller;

import com.demo.user.dto.ApiResponse;
import com.demo.user.dto.LoginRequest;
import com.demo.user.model.User;
import com.demo.user.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	/**
	 * curl -X POST http://localhost:8081/api/users/login \
	 *   -H "Content-Type: application/json" \
	 *   -d '{"email":"alice@example.com","password":"password123"}'
	 * <br>
	 * the '-N' means to Disable buffering of the output stream, so that the response is sent to the client as soon as it is available.
	 * NOTE: The url incldes '&', so it should be wrapped in single quotes to avoid shell interpretation issues.
	 * @return specify more platforms, such as ollama, openai, etc.
	 */
	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody LoginRequest request) {
		return userService.login(request.getEmail(), request.getPassword())
				.map(token -> ResponseEntity.ok(Map.of("token", token))).orElse(ResponseEntity.status(401).build());
	}

	@PostMapping("/register")
	public ResponseEntity<User> register(@RequestBody User user) {
		return ResponseEntity.ok(userService.createUser(user));
	}

	/**
	 * curl -X GET http://localhost:8081/api/users
	 */
	@GetMapping
	public ResponseEntity<List<User>> getAllUsers() {
		return ResponseEntity.ok(userService.getAllUsers());
	}

	@GetMapping("/paged")
	public ResponseEntity<ApiResponse<Page<User>>> getUsersPaged(@RequestParam(defaultValue = "0") int page,
                                                               @RequestParam(defaultValue = "5") int size) {
		return ResponseEntity.ok(ApiResponse.success(userService.getUsersPaged(page, size)));
	}

	/**
	 * curl -X GET http://localhost:8081/api/users/1
	 */
	@GetMapping("/{id}")
	public ResponseEntity<User> getUserById(@PathVariable(/* "id" */) @NonNull Long id) {
		return ResponseEntity.ok(userService.getUserById(id));
		/*
		 * userService.getUserById(id) .map(ResponseEntity::ok)
		 * .orElse(ResponseEntity.notFound().build());
		 */
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteUser(@PathVariable(/* "id" */) @NonNull Long id) {
		userService.deleteUser(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/health")
	public ResponseEntity<String> health() {
		return ResponseEntity.ok("User Service is running");
	}
}