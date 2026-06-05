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
        String[] products = {"Laptop", "Mouse", "Keyboard", "Monitor",
                "Headphones", "Webcam", "USB Hub", "SSD Drive",
                "Graphics Card", "Motherboard", "RAM", "CPU Cooler",
                "Power Supply", "PC Case", "Speakers"};
        String[] statuses = {"COMPLETED", "PENDING", "PROCESSING"};

        // Add 30 orders
        for (int i = 0; i < 30; i++) {
            String product = products[i % products.length];
            String status = statuses[i % statuses.length];
            Long userId = (long) (i % 3 + 1);
            Double amount = Math.round((50 + Math.random() * 950) * 100.0) / 100.0;
            orderRepository.save(new Order(userId, product, amount, status));
        }
    }
}