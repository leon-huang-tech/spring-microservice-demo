package com.demo.order.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.demo.order.exception.ResourceNotFoundException;
import com.demo.order.model.Order;
import com.demo.order.repository.OrderRepository;

@Service
public class OrderService {

	private static final Logger log = LoggerFactory.getLogger(OrderService.class);
	
    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Cacheable(value = "orders")
    public List<Order> getAllOrders() {
    	log.info(">>> Fetching orders from DATABASE");
        return orderRepository.findAll();
    }

    @Cacheable(value = "orders", key = "#id")
    public Order getOrderById(Long id) {
    	log.info(">>> Fetching order {} from DATABASE", id);
    	return orderRepository.findById(id)
		.orElseThrow(() ->
				new ResourceNotFoundException("Order not found with id: " + id));
//        return orderRepository.findById(id);
    }

    @Cacheable(value = "orders", key = "'user_' + #userId")
    public List<Order> getOrdersByUserId(Long userId) {
    	log.info(">>> Fetching orders for user {} from DATABASE", userId);
        return orderRepository.findByUserId(userId);
    }

    @CacheEvict(value = "orders", allEntries = true)
    public Order createOrder(Order order) {
    	
        return orderRepository.save(order);
    }

    @CacheEvict(value = "orders", allEntries = true)
    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }
}
