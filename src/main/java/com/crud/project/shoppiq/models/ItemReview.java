package com.crud.project.shoppiq.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity
@Table(name = "item_review")
public class ItemReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;

    @Column
    private String username;
    @Column
    private String review;

    @ManyToOne
    @JoinColumn(name = "items_id")
    @JsonBackReference
    private Item item;

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }



    public void update(ItemReview itemReview) {
        this.username = itemReview.username;
        this.review = itemReview.review;
    }
}
