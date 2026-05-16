package com.crud.project.shoppiq.controllers;

import com.crud.project.shoppiq.models.Order;
import com.crud.project.shoppiq.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @PostMapping("/create")
    public ResponseEntity<Optional<Order>> saveItem(@RequestBody Order newOrder) {
        Optional<Order> order = orderService.saveNewOrder(newOrder);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<Optional<Order>> getItemById(@PathVariable long id) {
        Optional<Order> order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteItem(@PathVariable long id) {
        orderService.deleteOrderById(id);
        return ResponseEntity.ok("deleted item with id: " + id);
    }
}
