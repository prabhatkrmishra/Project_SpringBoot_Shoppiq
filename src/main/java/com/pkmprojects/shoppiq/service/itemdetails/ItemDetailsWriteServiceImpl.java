package com.pkmprojects.shoppiq.service.itemdetails;

import com.pkmprojects.shoppiq.entity.item.ItemDetails;
import com.pkmprojects.shoppiq.repository.item.ItemDetailsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * {@link ItemDetailsWriteService} implementation providing transactional persistence
 * for item-details entities.
 *
 * @author prabhatkrmishra
 * @see ItemDetailsWriteService
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
