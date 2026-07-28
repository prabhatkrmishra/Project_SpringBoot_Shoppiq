package com.pkmprojects.shoppiq.service.item;

import com.pkmprojects.shoppiq.entity.item.Item;
import com.pkmprojects.shoppiq.repository.item.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <strong>Spring Boot Concept:</strong> Implementation of {@link ItemWriteService}
 * providing transactional persistence for item entities.
 *
 * <p>Thin write facade that delegates save and delete to {@code ItemRepository}.
 * When called from {@link ItemServiceImpl}, transactions propagate via Spring's
 * default REQUIRED propagation.</p>
 *
 * <p>Why this design:
 * <ul>
 *   <li><strong>@Service</strong> — Spring stereotype for service-layer beans, auto-detected via component scanning.</li>
 *   <li><strong>@Transactional</strong> — Each save/delete operation is individually transactional.</li>
 *   <li><strong>@RequiredArgsConstructor</strong> — Lombok-generated constructor injection for final fields.</li>
 * </ul>
 * </p>
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
