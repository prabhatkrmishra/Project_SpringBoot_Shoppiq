package com.pkmprojects.shoppiq.service.item;

import com.pkmprojects.shoppiq.entity.item.Item;
import com.pkmprojects.shoppiq.repository.item.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@link ItemWriteService} implementation providing transactional persistence
 * for item entities.
 *
 * @author prabhatkrmishra
 * @see ItemWriteService
 * @since 1.4.0
 */
@Service
@RequiredArgsConstructor
class ItemWriteServiceImpl implements ItemWriteService {

    private final ItemRepository itemRepository;

    /**
     * Persists the given item entity.
     *
     * @param item the item entity to save
     * @return the saved item entity
     */
    @Override
    @Transactional
    public Item save(Item item) {
        return itemRepository.save(item);
    }

    /**
     * Deletes the given item entity.
     *
     * @param item the item entity to delete
     */
    @Override
    @Transactional
    public void delete(Item item) {
        itemRepository.delete(item);
    }
}
