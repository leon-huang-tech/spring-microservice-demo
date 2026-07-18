package com.demo.user.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.demo.user.dao.LoginRequest;
import com.demo.user.model.User;
import com.demo.user.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody LoginRequest request) {
		return userService.login(request.getEmail(), request.getPassword())
				.map(token -> ResponseEntity.ok(Map.of("token", token))).orElse(ResponseEntity.status(401).build());
	}

	@PostMapping("/register")
	public ResponseEntity<User> register(@RequestBody User user) {
		return ResponseEntity.ok(userService.createUser(user));
	}

	@GetMapping
	public ResponseEntity<List<User>> getAllUsers() {
		return ResponseEntity.ok(userService.getAllUsers());
	}

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