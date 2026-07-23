package com.demo.user;

import com.demo.user.model.Role;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.demo.user.model.User;
import com.demo.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    public void run(String @NonNull ... args) {
        final String[] USER_NAMES = {
         "Alice", "Bob", "Charlie", "David", "Emma", "Frank", "Grace", "Henry", "Isabella", "Jack",
         "Kevin", "Liam", "Mia", "Noah", "Olivia", "Peter", "Quinn", "Ryan", "Sophia", "Thomas",
         "Uma", "Victoria", "William", "Xavier", "Yvonne", "Zachary", "Daniel", "Ethan", "Lucas", "Mason"
        };
        String encodedPwd = passwordEncoder.encode("password123");

        List<User> users = new ArrayList<>(
         Arrays.stream(USER_NAMES)
          .map(name -> new User(
           name,
           name.toLowerCase() + "@example.com",
           encodedPwd,
           Role.USER
          ))
          .toList()
        );

        User adminUser = new User("Admin", "admin@example.com", "password123", Role.ADMIN);
        users.add(adminUser);

        userRepository.saveAll(users);

    }
}