package com.crud.project.shoppiq.services;

import com.crud.project.shoppiq.models.Item;
import com.crud.project.shoppiq.repositories.ItemRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ItemService {

    @Autowired
    private ItemRepository itemRepository;

    @Transactional
    public Optional<Item> saveNewItem(Item newItem) {
        return Optional.ofNullable(itemRepository.save(newItem));
    }

    @Transactional
    public Optional<Item> getItemById(int id) {
        return itemRepository.findById(id);
    }

    @Transactional
    public void deleteItemById(int id) {
        itemRepository.deleteById(id);
    }

    @Transactional
    public Optional<Item> updateItemById(int id, Item newItem) {
        return itemRepository.updateById(id, newItem);
    }
}
