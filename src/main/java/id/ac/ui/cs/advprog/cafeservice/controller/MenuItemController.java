package id.ac.ui.cs.advprog.cafeservice.controller;

import id.ac.ui.cs.advprog.cafeservice.dto.MenuItemRequest;
import id.ac.ui.cs.advprog.cafeservice.exceptions.BadRequest;
import id.ac.ui.cs.advprog.cafeservice.exceptions.MenuItemValueEmpty;
import id.ac.ui.cs.advprog.cafeservice.exceptions.MenuItemValueInvalid;
import id.ac.ui.cs.advprog.cafeservice.model.menu.MenuItem;
import id.ac.ui.cs.advprog.cafeservice.service.MenuItemService;
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

    @GetMapping("/all")
    public ResponseEntity<List<MenuItem>> getAllMenuItem() {
        List<MenuItem> response = menuItemService.findAll();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<MenuItem> getMenuItemById(@PathVariable String id) {
        MenuItem response = menuItemService.findById(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/create")
    public ResponseEntity<MenuItem> addMenuItem(@RequestBody MenuItemRequest request) {

        if(request.getName() == null || request.getPrice() == null || request.getStock() == null){
            throw new BadRequest();
        }

        else if(request.getName().equals("")){
            throw new MenuItemValueEmpty("Name");
        }

        else if(request.getPrice() < 0){
            throw new MenuItemValueInvalid("Price");
        }

        else if(request.getStock() < 0){
            throw new MenuItemValueInvalid("Stock");
        }

        else{
            MenuItem response = menuItemService.create(request);
            return ResponseEntity.ok(response);
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<MenuItem> putMenuItem(@PathVariable String id, @RequestBody MenuItemRequest request) {
        MenuItem response = menuItemService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteMenuItem(@PathVariable String id) {
        menuItemService.delete(id);
        return ResponseEntity.ok("Deleted Menu Item with id " + id);
    }
}
