package com.pkmprojects.shoppiq.controller;

import com.pkmprojects.shoppiq.entity.Item;
import com.pkmprojects.shoppiq.service.ItemService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/item")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping("/create")
    public ResponseEntity<Item> saveItem(@RequestBody Item newItem) {
        Item item = itemService.saveNewItem(newItem);
        return ResponseEntity.status(HttpStatus.CREATED).body(item);
    }

    @PostMapping("/create/bulk")
    public ResponseEntity<List<Item>> saveItem(@RequestBody List<Item> newItems) {
        List<Item> items = itemService.saveItemBulk(newItems);
        return ResponseEntity.status(HttpStatus.CREATED).body(items);
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<Item> getItemById(@PathVariable long id) {
        Item item = itemService.getItemById(id);
        return ResponseEntity.ok(item);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Item>> getAllItems() {
        List<Item> items = itemService.getAllExistingItems();
        return ResponseEntity.ok(items);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable long id) {
        itemService.deleteItemById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Item> updateItem(@PathVariable long id, @RequestBody Item newItem) {
        Item item = itemService.updateItemById(id, newItem);
        return ResponseEntity.ok(item);
    }
}
