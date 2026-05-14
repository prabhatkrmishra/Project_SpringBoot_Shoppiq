package com.crud.project.shoppiq.repositories;

import com.crud.project.shoppiq.exceptions.ItemNotFoundException;
import com.crud.project.shoppiq.models.Item;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ItemRepository implements RepositoryInterface<Item> {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Item save(Item entity) {
        Session session = entityManager.unwrap(Session.class);
        // Inserts generated id into it
        session.persist(entity);
        return entity;
    }

    @Override
    public List<Item> saveAll(List<Item> entities) {
        return List.of();
    }

    @Override
    public Optional<Item> findById(int id) {
        Session session = entityManager.unwrap(Session.class);

        Optional<Item> optionalItem = Optional.ofNullable(session.find(Item.class, id));

        if (optionalItem.isPresent()) {
            return optionalItem;
        }

        throw new ItemNotFoundException("Item with id: " + id + " not found");
    }

    @Override
    public boolean existsById(int id) {
        return false;
    }

    @Override
    public List<Item> findAll() {
        return List.of();
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public Optional<Item> updateById(int id, Item entity) {
        Session session = entityManager.unwrap(Session.class);

        Optional<Item> optionalItem = Optional.ofNullable(session.find(Item.class, id));

        if (optionalItem.isPresent()) {
            optionalItem.get().update(entity);
            session.merge(optionalItem.get());
            return optionalItem;
        }

        throw new ItemNotFoundException("Item with id: " + id + " not found");
    }

    @Override
    public void delete(Item entity) {

    }

    @Override
    public void deleteById(int id) {

        Session session = entityManager.unwrap(Session.class);

        Optional<Item> optionalItem = Optional.ofNullable(session.find(Item.class, id));

        if (optionalItem.isPresent()) {
            session.remove(optionalItem.get());
            return;
        }

        throw new ItemNotFoundException("Item with id: " + id + " not found");
    }

    @Override
    public void deleteAll() {

    }
}
