package com.pkmprojects.shoppiq.service;

import com.pkmprojects.shoppiq.exception.OrderNotFoundException;
import com.pkmprojects.shoppiq.entity.Item;
import com.pkmprojects.shoppiq.entity.Order;
import com.pkmprojects.shoppiq.repository.ItemRepository;
import com.pkmprojects.shoppiq.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ItemRepository itemRepository;

    public Optional<Order> saveNewOrder(Order newOrder) {
        Order saveOrder = new Order();
        List<Item> items = new ArrayList<>();

        saveOrder.setType(newOrder.getType());

        for (Item item : newOrder.getItemList()) {
            Optional<Item> currentItem = itemRepository.findById(item.getId());
            currentItem.ifPresent(items::add);
        }

        saveOrder.setItemList(items);

        return Optional.of(orderRepository.save(saveOrder));
    }

    public Optional<Order> getOrderById(long orderId) {
        Optional<Order> currentOrder = orderRepository.findById(orderId);
        if (currentOrder.isPresent()) {
            return currentOrder;
        }

        throw new OrderNotFoundException("order with id: " + orderId + " not found");
    }

    public Optional<List<Order>> getAllExistingOrders() {
        List<Order> orderList = new ArrayList<>();
        orderRepository.findAll().forEach(orderList::add);
        return Optional.of(orderList);
    }

    public void deleteOrderById(long orderId) {
        Optional<Order> currentOrder = orderRepository.findById(orderId);
        if (currentOrder.isPresent()) {
            orderRepository.deleteById(orderId);
            return;
        }

        throw new OrderNotFoundException("order with id: " + orderId + " not found");
    }
}
