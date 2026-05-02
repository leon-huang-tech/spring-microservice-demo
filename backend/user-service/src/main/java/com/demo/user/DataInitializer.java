package com.demo.user;

import com.demo.user.model.User;
import com.demo.user.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        userRepository.save(new User("Alice", "alice@example.com",
                passwordEncoder.encode("password123")));
        userRepository.save(new User("Bob", "bob@example.com",
                passwordEncoder.encode("password123")));
        userRepository.save(new User("Charlie", "charlie@example.com",
                passwordEncoder.encode("password123")));
    }
}