package com.demo.user.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.demo.user.exception.ResourceNotFoundException;
import com.demo.user.model.User;
import com.demo.user.repository.UserRepository;
import com.demo.user.security.JwtService;


@Service
public class UserService {

	private static final Logger log = LoggerFactory.getLogger(UserService.class);
	
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Cacheable(value = "users")	
    public List<User> getAllUsers() {
    	// when second time, it will fetch from cache, so we can see the log only once
    	log.info(">>> Fetching users from DATABASE");
        return userRepository.findAll();
    }

	/*
	 * public Optional<User> getUserById(Long id) { return
	 * userRepository.findById(id); }
	 */
    @Cacheable(value = "users", key = "#id")
    public User getUserById(Long id) {
        return userRepository.findById(id)
        		.orElseThrow(() ->
        				new ResourceNotFoundException("User not found with id: " + id));
    }

    /**
     * Evict the cache for all users since we don't know which user is updated
     * For cache consistency
     * @param user
     * @return
     */
    @CacheEvict(value = "users", allEntries = true)
    public User createUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public Optional<String> login(String email, String password) {
        return userRepository.findByEmail(email)
                .filter(user -> passwordEncoder.matches(password, user.getPassword()))
                .map(user -> jwtService.generateToken(user.getEmail()));
    }


    /**
     * Evict the cache for the updated user
     * Deletes a user by their ID.
     * For cache consistency
     * @param id
     */
    @CacheEvict(value = "users", allEntries = true)
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}