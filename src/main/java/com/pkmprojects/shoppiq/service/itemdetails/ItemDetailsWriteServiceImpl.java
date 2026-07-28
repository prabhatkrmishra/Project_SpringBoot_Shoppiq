package com.pkmprojects.shoppiq.service.itemdetails;

import com.pkmprojects.shoppiq.entity.item.ItemDetails;
import com.pkmprojects.shoppiq.repository.item.ItemDetailsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <strong>Spring Boot Concept:</strong> Default implementation of {@link ItemDetailsWriteService}.
 *
 * <h2>What is {@code @Service}?</h2>
 * <p>
 * Spring Stereotype registering this package-private class as a bean.
 * </p>
 *
 * <h2>What is {@code @RequiredArgsConstructor}?</h2>
 * <p>
 * Lombok-generated constructor for injecting {@code ItemDetailsRepository}.
 * </p>
 *
 * <h2>What is {@code @Transactional}?</h2>
 * <p>
 * Each save operation is transactional. When called from {@code InventoryServiceImpl}
 * or {@code AdminInventoryServiceImpl}, the transaction propagates to the caller's
 * transaction (Spring {@code REQUIRED} propagation).
 * </p>
 *
 * @author prabhatkrmishra
 * @since 1.4.0
 */
@Service
@RequiredArgsConstructor
class ItemDetailsWriteServiceImpl implements ItemDetailsWriteService {

    private final ItemDetailsRepository itemDetailsRepository;

    /**
     * Persists the given item details entity.
     *
     * <p>When called from {@code InventoryServiceImpl} or
     * {@code AdminInventoryServiceImpl}, the transaction propagates
     * to the caller's transaction via Spring's REQUIRED propagation.</p>
     *
     * @param itemDetails the item details entity to save
     * @return the saved item details entity
     */
    @Override
    @Transactional
    public ItemDetails save(ItemDetails itemDetails) {
        return itemDetailsRepository.save(itemDetails);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Uses {@code saveAll()} for a single JDBC batch, reducing round-trips.</p>
     */
    @Override
    @Transactional
    public List<ItemDetails> saveAll(List<ItemDetails> itemDetailsList) {
        return itemDetailsRepository.saveAll(itemDetailsList);
    }
}
