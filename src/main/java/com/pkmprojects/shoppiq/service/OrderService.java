package com.pkmprojects.shoppiq.service;

import com.pkmprojects.shoppiq.entity.Item;
import com.pkmprojects.shoppiq.entity.Order;
import com.pkmprojects.shoppiq.exception.OrderNotFoundException;
import com.pkmprojects.shoppiq.repository.ItemRepository;
import com.pkmprojects.shoppiq.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for Order CRUD operations.
 *
 * <p>
 * Methods that look up a single Order throw {@link OrderNotFoundException}
 * when the resource does not exist, rather than returning an empty
 * {@code Optional} for the caller to re-check.
 * </p>
 */
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;

    public OrderService(OrderRepository orderRepository, ItemRepository itemRepository) {
        this.orderRepository = orderRepository;
        this.itemRepository = itemRepository;
    }

    public Order saveNewOrder(Order newOrder) {
        Order saveOrder = new Order();
        List<Item> items = new ArrayList<>();

        saveOrder.setType(newOrder.getType());

        for (Item item : newOrder.getItemList()) {
            itemRepository.findById(item.getId()).ifPresent(items::add);
        }

        saveOrder.setItemList(items);

        return orderRepository.save(saveOrder);
    }

    public Order getOrderById(long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("order with id: " + orderId + " not found"));
    }

    public List<Order> getAllExistingOrders() {
        List<Order> orderList = new ArrayList<>();
        orderRepository.findAll().forEach(orderList::add);
        return orderList;
    }

    public void deleteOrderById(long orderId) {
        if (!orderRepository.existsById(orderId)) {
            throw new OrderNotFoundException("order with id: " + orderId + " not found");
        }
        orderRepository.deleteById(orderId);
    }
}
