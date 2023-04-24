package id.ac.ui.cs.advprog.cafeservice.service.menu;

import id.ac.ui.cs.advprog.cafeservice.dto.MenuItemRequest;
import id.ac.ui.cs.advprog.cafeservice.exceptions.MenuItemDoesNotExistException;
import id.ac.ui.cs.advprog.cafeservice.model.menu.MenuItem;
import id.ac.ui.cs.advprog.cafeservice.repository.MenuItemRepository;
import id.ac.ui.cs.advprog.cafeservice.service.MenuItemServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MenuItemServiceImplTest {

    @InjectMocks
    private MenuItemServiceImpl service;

    @Mock
    private MenuItemRepository repository;

    MenuItem menuItem;

    MenuItem newMenuItem;

    MenuItemRequest createRequest;

    MenuItemRequest updateRequest;

    @BeforeEach
    void setUp() {
        createRequest = MenuItemRequest.builder()
                .name("Indomie")
                .price(5000)
                .stock(100)
                .build();

        updateRequest = MenuItemRequest.builder()
                .name("Es Teh")
                .price(2000)
                .stock(200)
                .build();

        menuItem = MenuItem.builder()
                .id("7dd3fd7a-4952-4eb2-8ba0-bbe1767b4a10")
                .name("Indomie")
                .price(5000)
                .stock(100)
                .build();

        newMenuItem = MenuItem.builder()
                .id("7dd3fd7a-4952-4eb2-8ba0-bbe1767b4a10")
                .name("Es Teh")
                .price(2000)
                .stock(200)
                .build();
    }

    @Test
    void whenFindAllMenuItemShouldReturnListOfMenuItem() {
        List<MenuItem> allMenuItem = List.of(menuItem);

        when(repository.findAll()).thenReturn(allMenuItem);

        List<MenuItem> result = service.findAll(null);
        verify(repository, atLeastOnce()).findAll();
        Assertions.assertEquals(allMenuItem, result);
    }

    @Test
    void whenFindAllMenuItemWithQueryShouldReturnListOfAvailableMenuItem() {
        List<MenuItem> availableMenuItems = List.of(menuItem);

        when(repository.findByStockGreaterThan(anyInt())).thenReturn(availableMenuItems);

        List<MenuItem> result = service.findAll("available");
        verify(repository, atLeastOnce()).findByStockGreaterThan(0);
        Assertions.assertEquals(availableMenuItems, result);
    }

    @Test
    void whenFindByIdAndFoundShouldReturnMenuItem(){
        when(repository.findById(any(String.class))).thenReturn(Optional.of(menuItem));

        MenuItem result = service.findById("7dd3fd7a-4952-4eb2-8ba0-bbe1767b4a10");
        verify(repository, atLeastOnce()).findById(any(String.class));
        Assertions.assertEquals(menuItem, result);
    }

    @Test
    void whenFindByIdAndNotFoundShouldThrowException() {
        when(repository.findById(any(String.class))).thenReturn(Optional.empty());

        Assertions.assertThrows(MenuItemDoesNotExistException.class, () -> service.findById("7dd3fd7a-4952-4eb2-8ba0-bbe1767b4a11"));
    }

    @Test
    void whenCreateMenuItemShouldReturnTheCreatedMenuItem() {
        when(repository.save(any(MenuItem.class))).thenAnswer(invocation -> {
            var menuItem = invocation.getArgument(0, MenuItem.class);
            menuItem.setId("7dd3fd7a-4952-4eb2-8ba0-bbe1767b4a10");
            return menuItem;
        });

        MenuItem result = service.create(createRequest);
        verify(repository, atLeastOnce()).save(any(MenuItem.class));
        Assertions.assertEquals(menuItem, result);
    }

    @Test
    void whenUpdateMenuItemAndFoundShouldReturnTheUpdatedMenuItem() {
        when(repository.findById(any(String.class))).thenReturn(Optional.of(menuItem));
        when(repository.save(any(MenuItem.class))).thenAnswer(invocation ->
                invocation.getArgument(0, MenuItem.class));

        MenuItem result = service.update("7dd3fd7a-4952-4eb2-8ba0-bbe1767b4a10", updateRequest);
        verify(repository, atLeastOnce()).save(any(MenuItem.class));
        Assertions.assertEquals(newMenuItem, result);
    }

    @Test
    void whenUpdateMenuItemAndNotFoundShouldThrowException() {
        when(repository.findById(any(String.class))).thenReturn(Optional.empty());
        Assertions.assertThrows(MenuItemDoesNotExistException.class, () -> service.update("f20a0089-a4d6-49d7-8be8-9cdc81bd7341", updateRequest));
    }

    @Test
    void whenDeleteMenuItemAndFoundShouldCallDeleteByIdOnRepo() {
        when(repository.findById(any(String.class))).thenReturn(Optional.of(menuItem));

        service.delete("f20a0089-a4d6-49d7-8be8-9cdc81bd7341");
        verify(repository, atLeastOnce()).deleteById(any(String.class));
    }

    @Test
    void whenDeleteMenuItemAndNotFoundShouldThrowException() {
        when(repository.findById(any(String.class))).thenReturn(Optional.empty());
        Assertions.assertThrows(MenuItemDoesNotExistException.class, () -> {
            service.delete("f20a0089-a4d6-49d7-8be8-9cdc81bd7341");
        });
    }
}
