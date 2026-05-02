package com.demo.order;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.demo.order.model.Order;
import com.demo.order.repository.OrderRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private final OrderRepository orderRepository;

    public DataInitializer(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public void run(String... args) {
        orderRepository.save(new Order(1L, "Laptop", 999.99, "COMPLETED"));
        orderRepository.save(new Order(1L, "Mouse", 29.99, "PENDING"));
        orderRepository.save(new Order(2L, "Keyboard", 79.99, "COMPLETED"));
        orderRepository.save(new Order(3L, "Monitor", 399.99, "PROCESSING"));
    }
}