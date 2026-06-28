package com.pkmprojects.shoppiq.controller;

import com.pkmprojects.shoppiq.entity.Order;
import com.pkmprojects.shoppiq.service.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/create")
    public ResponseEntity<Order> saveItem(
            @Valid @RequestBody Order newOrder) {

        Order order = orderService.saveNewOrder(newOrder);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<Order> getItemById(
            @PathVariable
            @Positive(message = "Order id must be a positive number")
            long id) {

        Order order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Order>> getAllOrders() {
        List<Order> orders = orderService.getAllExistingOrders();
        return ResponseEntity.ok(orders);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteItem(
            @PathVariable
            @Positive(message = "Order id must be a positive number")
            long id) {

        orderService.deleteOrderById(id);
        return ResponseEntity.noContent().build();
    }
}
