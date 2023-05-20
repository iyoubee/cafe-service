package id.ac.ui.cs.advprog.cafeservice.controller;

import id.ac.ui.cs.advprog.cafeservice.dto.MenuItemRequest;
import id.ac.ui.cs.advprog.cafeservice.model.menu.MenuItem;
import id.ac.ui.cs.advprog.cafeservice.service.MenuItemService;
import id.ac.ui.cs.advprog.cafeservice.validator.MenuItemValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cafe/menu")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MenuItemController {
    private final MenuItemService menuItemService;

    private final MenuItemValidator menuItemValidator;

    @GetMapping("/all")
    public ResponseEntity<List<MenuItem>> getAllMenuItem(@RequestParam(required = false) String query) {
        List<MenuItem> response = menuItemService.findAll(query);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<MenuItem> getMenuItemById(@PathVariable String id) {
        MenuItem response = menuItemService.findById(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/create")
    public ResponseEntity<MenuItem> addMenuItem(@RequestBody MenuItemRequest request) {
        menuItemValidator.validateRequest(request);
        MenuItem response = menuItemService.create(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<MenuItem> putMenuItem(@PathVariable String id, @RequestBody MenuItemRequest request) {
        menuItemValidator.validateRequest(request);
        MenuItem response = menuItemService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteMenuItem(@PathVariable String id) {
        menuItemService.delete(id);
        return ResponseEntity.ok("Deleted Menu Item with id " + id);
    }

}
