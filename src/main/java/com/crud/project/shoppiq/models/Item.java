package com.crud.project.shoppiq.models;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "items")
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;
    @Column
    private String name;
    @Column
    private String description;

    @OneToOne(cascade = CascadeType.ALL)
    private ItemDetails itemDetails;

    // Mapped by item obj in ItemReview
    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL)
    // @JoinColumn(name = "items_id") now in review
    @JsonManagedReference
    private List<ItemReview> itemReviews;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ItemDetails getItemDetails() {
        return itemDetails;
    }

    public void setItemDetails(ItemDetails itemDetails) {
        this.itemDetails = itemDetails;
    }

    public void update(Item newItem) {
        this.name = newItem.getName();
        this.description = newItem.getDescription();

        if (newItem.getItemDetails() != null) {

            if (this.itemDetails == null) {
                this.itemDetails = new ItemDetails();
            }

            this.itemDetails.update(
                    newItem.getItemDetails()
            );
        }
    }

    public List<ItemReview> getItemReviews() {
        return itemReviews;
    }

    public void setItemReviews(List<ItemReview> itemReviews) {
        this.itemReviews = itemReviews;
    }
}
