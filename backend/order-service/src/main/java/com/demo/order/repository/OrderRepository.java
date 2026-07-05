package com.demo.order.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.demo.order.model.Order;

//@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
	List<Order> findByUserId(Long userId);
}