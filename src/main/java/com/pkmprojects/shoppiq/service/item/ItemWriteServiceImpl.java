package com.pkmprojects.shoppiq.service.item;

import com.pkmprojects.shoppiq.entity.item.Item;
import com.pkmprojects.shoppiq.repository.item.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default implementation of {@link ItemWriteService}.
 *
 * @author PrabhatKrMishra
 * @since 1.4.0
 */
@Service
@RequiredArgsConstructor
class ItemWriteServiceImpl implements ItemWriteService {

    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public Item save(Item item) {
        return itemRepository.save(item);
    }

    @Override
    @Transactional
    public void delete(Item item) {
        itemRepository.delete(item);
    }
}
