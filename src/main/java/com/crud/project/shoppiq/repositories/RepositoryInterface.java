package com.crud.project.shoppiq.repositories;

import com.crud.project.shoppiq.models.ItemReview;

import java.util.List;
import java.util.Optional;

public interface RepositoryInterface<T> {

    T save(T entity);

    List<T> saveAll(List<T> entities);

    Optional<T> findById(long id);

    boolean existsById(long id);

    List<T> findAll();

    long count();

    Optional<T> updateById(long id, T t);

    void delete(T entity);

    void deleteById(long id);

    void deleteAll();
}