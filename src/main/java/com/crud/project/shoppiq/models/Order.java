package com.crud.project.shoppiq.models;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;

    @Column
    private String type;

    @ManyToMany()
    // In new table woth orders mapped with item id, col will be of orders id
    // and col 2 will be of items id joind inversly
    @JoinTable(name = "order_with_items", joinColumns = @JoinColumn(name = "orders_id"),
            inverseJoinColumns = @JoinColumn(name = "items_id"))
    private List<Item> itemList;

    public Long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Item> getItemList() {
        return itemList;
    }

    public void setItemList(List<Item> items) {
        this.itemList = items;
    }
}
