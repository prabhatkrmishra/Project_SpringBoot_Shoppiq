package com.crud.project.shoppiq.repositories;

import com.crud.project.shoppiq.exceptions.ItemNotFoundException;
import com.crud.project.shoppiq.exceptions.ItemReviewNotFoundException;
import com.crud.project.shoppiq.models.ItemReview;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ItemReviewRepository implements RepositoryInterface<ItemReview> {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public ItemReview save(ItemReview entity) {
        return null;
    }

    public ItemReview saveWithItemId(ItemReview newItemReview) {
        Session session = entityManager.unwrap(Session.class);
        session.persist(newItemReview);
        return newItemReview;
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

        throw new ItemReviewNotFoundException("item review with id: " + id + " not found");
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
    public Optional<ItemReview> updateById(long reviewId, ItemReview itemReview) {
        Session session = entityManager.unwrap(Session.class);

        Optional<ItemReview> optionalItem = Optional.ofNullable(session.find(ItemReview.class, reviewId));

        if (optionalItem.isPresent()) {
            optionalItem.get().update(itemReview);
            session.merge(optionalItem.get());
            return optionalItem;
        }

        throw new ItemNotFoundException("item review with id: " + itemReview + " not found");
    }

    @Override
    public void delete(ItemReview entity) {

    }

    @Override
    public void deleteById(long id) {
        Session session = entityManager.unwrap(Session.class);

        Optional<ItemReview> optionalItem = Optional.ofNullable(session.find(ItemReview.class, id));

        if (optionalItem.isPresent()) {
            session.remove(optionalItem.get());
            return;
        }

        throw new ItemReviewNotFoundException("item review with id: " + id + " not found");
    }

    @Override
    public void deleteAll() {

    }
}
