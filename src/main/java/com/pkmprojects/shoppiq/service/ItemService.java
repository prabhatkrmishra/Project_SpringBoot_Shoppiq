package com.pkmprojects.shoppiq.service;

import com.pkmprojects.shoppiq.entity.Item;
import com.pkmprojects.shoppiq.exception.ItemNotFoundException;
import com.pkmprojects.shoppiq.repository.ItemRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for Item CRUD operations.
 *
 * <p>
 * Methods that look up a single Item throw {@link ItemNotFoundException}
 * when the resource does not exist rather than returning an empty
 * {@code Optional}. This lets {@code GlobalExceptionHandler} translate
 * missing-resource cases into a consistent RFC 9457 response and removes
 * the need for callers to unwrap an {@code Optional} that, by this point,
 * could never actually be empty.
 * </p>
 */
@Service
public class ItemService {

    private final ItemRepository itemRepository;

    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public Item saveNewItem(Item newItem) {
        return itemRepository.save(newItem);
    }

    public List<Item> saveItemBulk(List<Item> newItems) {
        return itemRepository.saveAll(newItems);
    }

    public Item getItemById(long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("Item with id: " + id + " not found"));
    }

    public List<Item> getAllExistingItems() {
        List<Item> itemList = new ArrayList<>();
        itemRepository.findAll().forEach(itemList::add);
        return itemList;
    }

    public void deleteItemById(long id) {
        if (!itemRepository.existsById(id)) {
            throw new ItemNotFoundException("Item with id: " + id + " not found");
        }
        itemRepository.deleteById(id);
    }

    public Item updateItemById(long id, Item newItem) {
        Item currentItem = getItemById(id);
        currentItem.update(newItem);
        return itemRepository.save(currentItem);
    }
}
