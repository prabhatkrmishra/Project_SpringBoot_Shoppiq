package com.pkmprojects.shoppiq.controller;

import com.pkmprojects.shoppiq.entity.Order;
import com.pkmprojects.shoppiq.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/create")
    public ResponseEntity<Order> saveItem(@RequestBody Order newOrder) {
        Order order = orderService.saveNewOrder(newOrder);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<Order> getItemById(@PathVariable long id) {
        Order order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Order>> getAllOrders() {
        List<Order> orders = orderService.getAllExistingOrders();
        return ResponseEntity.ok(orders);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable long id) {
        orderService.deleteOrderById(id);
        return ResponseEntity.noContent().build();
    }
}
