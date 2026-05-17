package com.crud.project.shoppiq.repositories;

import com.crud.project.shoppiq.models.Item;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends CrudRepository<Item, Long> {

}
