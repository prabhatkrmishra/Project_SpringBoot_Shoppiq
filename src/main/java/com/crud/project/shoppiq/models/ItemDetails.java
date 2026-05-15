package com.crud.project.shoppiq.models;

import jakarta.persistence.*;

@Entity
@Table(name = "item_details")
public class ItemDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;

    @Column
    private String brand;

    @Column
    private double price;

    @Column
    private String category;

    public Long getId() {
        return id;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void update(ItemDetails itemDetails) {
        this.brand = itemDetails.getBrand();
        this.price = itemDetails.getPrice();
        this.category = itemDetails.getCategory();
    }
}
