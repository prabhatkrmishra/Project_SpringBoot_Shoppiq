package com.pkmprojects.shoppiq.aiservice.service;

import com.pkmprojects.shoppiq.entity.item.Item;
import com.pkmprojects.shoppiq.service.item.ItemLookupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Read-only implementation of {@link ChatProductService}.
 *
 * @author PrabhatKrMishra
 * @since 1.4.0
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class ChatProductServiceImpl implements ChatProductService {

    private final ItemLookupService itemLookupService;

    @Override
    public Optional<Item> findBySlug(String slug) {
        return itemLookupService.findBySlug(slug);
    }

    @Override
    public List<Item> findByNameContaining(String name, int limit) {
        return itemLookupService.findByNameContaining(name, 0, limit);
    }
}
