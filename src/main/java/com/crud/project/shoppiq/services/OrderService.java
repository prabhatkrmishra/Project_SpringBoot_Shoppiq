package com.crud.project.shoppiq.services;

import com.crud.project.shoppiq.models.Item;
import com.crud.project.shoppiq.models.Order;
import com.crud.project.shoppiq.repositories.ItemRepository;
import com.crud.project.shoppiq.repositories.OrderRepository;
import jakarta.transaction.Transactional;
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

    @Transactional
    public Optional<Order> saveNewOrder(Order newOrder) {
        Order saveOrder = new Order();
        List<Item> items = new ArrayList<>();

        saveOrder.setType(newOrder.getType());

        for (Item item : newOrder.getItemList()) {
            Optional<Item> currentItem = itemRepository.findById(item.getId());
            currentItem.ifPresent(items::add);
        }

        saveOrder.setItemList(items);

        return Optional.ofNullable(orderRepository.save(saveOrder));
    }

    @Transactional
    public Optional<Order> getOrderById(long id) {
        return orderRepository.findById(id);
    }

    @Transactional
    public void deleteOrderById(long id) {
        orderRepository.deleteById(id);
    }
}
