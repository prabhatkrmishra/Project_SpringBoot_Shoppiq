package com.pkmprojects.shoppiq.service.itemdetails;

import com.pkmprojects.shoppiq.entity.item.ItemDetails;
import com.pkmprojects.shoppiq.repository.item.ItemDetailsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default implementation of {@link ItemDetailsWriteService}.
 *
 * @author PrabhatKrMishra
 * @since 1.4.0
 */
@Service
@RequiredArgsConstructor
class ItemDetailsWriteServiceImpl implements ItemDetailsWriteService {

    private final ItemDetailsRepository itemDetailsRepository;

    @Override
    @Transactional
    public ItemDetails save(ItemDetails itemDetails) {
        return itemDetailsRepository.save(itemDetails);
    }
}
