package com.crud.project.shoppiq.repositories;

import java.util.List;
import java.util.Optional;

public interface RepositoryInterface<T> {

    T save(T entity);

    List<T> saveAll(List<T> entities);

    Optional<T> findById(int id);

    boolean existsById(int id);

    List<T> findAll();

    long count();

    Optional<T> updateById(int id, T t);

    void delete(T entity);

    void deleteById(int id);

    void deleteAll();
}