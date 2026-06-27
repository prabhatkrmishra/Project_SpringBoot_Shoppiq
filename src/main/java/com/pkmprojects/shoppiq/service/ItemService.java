package com.pkmprojects.shoppiq.service;

import com.pkmprojects.shoppiq.exception.ItemNotFoundException;
import com.pkmprojects.shoppiq.entity.Item;
import com.pkmprojects.shoppiq.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ItemService {

    @Autowired
    private ItemRepository itemRepository;

    public Optional<Item> saveNewItem(Item newItem) {
        return Optional.of(itemRepository.save(newItem));
    }

    public Optional<List<Item>> saveItemBulk(List<Item> newItems) {
        return Optional.of((List<Item>) itemRepository.saveAll(newItems));
    }

    public Optional<Item> getItemById(long id) {
        Optional<Item> currentItem = itemRepository.findById(id);
        if (currentItem.isPresent()) {
            return currentItem;
        }

        throw new ItemNotFoundException("Item with id: " + id + " not found");
    }

    public Optional<List<Item>> getAllExistingItems() {
        List<Item> itemList = new ArrayList<>();
        itemRepository.findAll().forEach(itemList::add);
        return Optional.of(itemList);
    }

    public void deleteItemById(long id) {
        Optional<Item> currentItem = itemRepository.findById(id);
        if (currentItem.isPresent()) {
            itemRepository.deleteById(id);
            return;
        }

        throw new ItemNotFoundException("Item with id: " + id + " not found");
    }

    public Optional<Item> updateItemById(long id, Item newItem) {
        Optional<Item> currentItem = itemRepository.findById(id);
        if (currentItem.isPresent()) {
            currentItem.get().update(newItem);
            itemRepository.save(currentItem.get());
            return currentItem;
        }

        throw new ItemNotFoundException("Item with id: " + id + " not found");
    }
}
