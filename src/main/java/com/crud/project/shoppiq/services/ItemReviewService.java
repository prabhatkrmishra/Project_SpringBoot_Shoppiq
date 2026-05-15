package com.crud.project.shoppiq.services;

import com.crud.project.shoppiq.models.ItemReview;
import com.crud.project.shoppiq.repositories.ItemReviewRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ItemReviewService {
    @Autowired
    private ItemReviewRepository itemReviewRepository;

    @Transactional
    public Optional<ItemReview> getReviewById(long id){
        return itemReviewRepository.findById(id);
    }
}
