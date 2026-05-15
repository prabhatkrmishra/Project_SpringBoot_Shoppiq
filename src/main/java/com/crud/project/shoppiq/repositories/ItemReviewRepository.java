package com.crud.project.shoppiq.repositories;

import com.crud.project.shoppiq.exceptions.ItemNotFoundException;
import com.crud.project.shoppiq.exceptions.ItemReviewNotFoundException;
import com.crud.project.shoppiq.models.Item;
import com.crud.project.shoppiq.models.ItemReview;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ItemReviewRepository implements RepositoryInterface<ItemReview>{
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public ItemReview save(ItemReview entity) {
        return null;
    }

    @Override
    public List<ItemReview> saveAll(List<ItemReview> entities) {
        return List.of();
    }

    @Override
    public Optional<ItemReview> findById(long id) {
        Session session = entityManager.unwrap(Session.class);

        Optional<ItemReview> optionalItem = Optional.ofNullable(session.find(ItemReview.class, id));

        if (optionalItem.isPresent()) {
            return optionalItem;
        }

        throw new ItemReviewNotFoundException("Item review with id: " + id + " not found");
    }

    @Override
    public boolean existsById(long id) {
        return false;
    }

    @Override
    public List<ItemReview> findAll() {
        return List.of();
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public Optional<ItemReview> updateById(long id, ItemReview itemReview) {
        return Optional.empty();
    }

    @Override
    public void delete(ItemReview entity) {

    }

    @Override
    public void deleteById(long id) {

    }

    @Override
    public void deleteAll() {

    }
}
