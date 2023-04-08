package id.ac.ui.cs.advprog.cafeservice.controller;

import id.ac.ui.cs.advprog.cafeservice.dto.MenuItemRequest;
import id.ac.ui.cs.advprog.cafeservice.model.menu.MenuItem;
import id.ac.ui.cs.advprog.cafeservice.service.MenuItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cafe/menu")
@RequiredArgsConstructor
public class MenuItemController {
    private final MenuItemService menuItemService;

    @GetMapping("/all")
    public ResponseEntity<List<MenuItem>> getAllMenuItem() {
        List<MenuItem> response = menuItemService.findAll();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<MenuItem> getMenuItemById(@PathVariable Integer id) {
        MenuItem response = menuItemService.findById(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/create")
    public ResponseEntity<MenuItem> addMenuItem(@RequestBody MenuItemRequest request) {
        MenuItem response = menuItemService.create(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<MenuItem> putMenuItem(@PathVariable Integer id, @RequestBody MenuItemRequest request) {
        MenuItem response = menuItemService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteMenuItem(@PathVariable Integer id) {
        menuItemService.delete(id);
        return ResponseEntity.ok(String.format("Deleted Menu Item with id %d", id));
    }
}
