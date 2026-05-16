package com.crud.project.shoppiq.repositories;

import com.crud.project.shoppiq.exceptions.ItemNotFoundException;
import com.crud.project.shoppiq.exceptions.ItemReviewNotFoundException;
import com.crud.project.shoppiq.exceptions.OrderNotFoundException;
import com.crud.project.shoppiq.models.Item;
import com.crud.project.shoppiq.models.ItemReview;
import com.crud.project.shoppiq.models.Order;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class OrderRepository implements RepositoryInterface<Order> {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Order save(Order entity) {
        Session session = entityManager.unwrap(Session.class);
        session.persist(entity);
        return entity;
    }

    @Override
    public List<Order> saveAll(List<Order> entities) {
        return List.of();
    }

    @Override
    public Optional<Order> findById(long id) {
        Session session = entityManager.unwrap(Session.class);

        Optional<Order> optionalItem = Optional.ofNullable(session.find(Order.class, id));

        if (optionalItem.isPresent()) {
            return optionalItem;
        }

        throw new OrderNotFoundException("order with id: " + id + " not found");
    }

    @Override
    public boolean existsById(long id) {
        return false;
    }

    @Override
    public List<Order> findAll() {
        return List.of();
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public Optional<Order> updateById(long id, Order order) {
        return Optional.empty();
    }

    @Override
    public void delete(Order entity) {

    }

    @Override
    public void deleteById(long id) {
        Session session = entityManager.unwrap(Session.class);

        Optional<Order> optionalItem = Optional.ofNullable(session.find(Order.class, id));

        if (optionalItem.isPresent()) {
            session.remove(optionalItem.get());
            return;
        }

        throw new OrderNotFoundException("order with id: " + id + " not found");
    }

    @Override
    public void deleteAll() {

    }
}
