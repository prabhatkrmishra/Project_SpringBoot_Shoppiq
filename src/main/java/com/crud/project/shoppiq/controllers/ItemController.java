package com.crud.project.shoppiq.controllers;

import com.crud.project.shoppiq.models.Item;
import com.crud.project.shoppiq.services.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/item")
public class ItemController {

    @Autowired
    private ItemService itemService;

    @PostMapping("/create")
    public ResponseEntity<Optional<Item>> saveItem(@RequestBody Item newItem) {
        Optional<Item> item = itemService.saveNewItem(newItem);
        return ResponseEntity.ok(item);
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<Optional<Item>> getItemById(@PathVariable long id) {
        Optional<Item> item = itemService.getItemById(id);
        return ResponseEntity.ok(item);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteItem(@PathVariable long id) {
        itemService.deleteItemById(id);
        return ResponseEntity.ok("deleted item with id: " + id);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Optional<Item>> updateItem(@PathVariable long id, @RequestBody Item newItem) {
        Optional<Item> item = itemService.updateItemById(id, newItem);
        return ResponseEntity.ok(item);
    }
}
