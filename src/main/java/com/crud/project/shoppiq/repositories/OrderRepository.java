package com.crud.project.shoppiq.repositories;

import com.crud.project.shoppiq.models.Order;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends CrudRepository<Order, Long> {

}
