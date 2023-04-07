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
                .name("Indome")
                .price(5000)
                .stock(100)
                .build();

        updateRequest = MenuItemRequest.builder()
                .name("Es Teh")
                .price(2000)
                .stock(200)
                .build();

        menuItem = MenuItem.builder()
                .id(0)
                .name("Indomie")
                .price(5000)
                .stock(100)
                .build();

        newMenuItem = MenuItem.builder()
                .id(0)
                .name("Es Teh")
                .price(2000)
                .stock(200)
                .build();
    }

    @Test
    void whenFindAllMenuItemShouldReturnListOfMenuItem() {
        List<MenuItem> allMenuItem = List.of(menuItem);

        when(repository.findAll()).thenReturn(allMenuItem);

        List<MenuItem> result = service.findAll();
        verify(repository, atLeastOnce()).findAll();
        Assertions.assertEquals(allMenuItem, result);
    }

    @Test
    void whenFindByIdAndFoundShouldReturnMenuItem(){
        when(repository.findById(any(Integer.class))).thenReturn(Optional.of(menuItem));

        MenuItem result = service.findById(0);
        verify(repository, atLeastOnce()).findById(any(Integer.class));
        Assertions.assertEquals(menuItem, result);
    }

    @Test
    void whenFindByIdAndNotFoundShouldThrowException() {
        when(repository.findById(any(Integer.class))).thenReturn(Optional.empty());

        Assertions.assertThrows(MenuItemDoesNotExistException.class, () -> service.findById(0));
    }

    @Test
    void whenCreateMenuItemShouldReturnTheCreatedMenuItem() {
        when(repository.save(any(MenuItem.class))).thenAnswer(invocation -> {
            var menuItem = invocation.getArgument(0, MenuItem.class);
            menuItem.setId(0);
            return menuItem;
        });

        MenuItem result = service.create(createRequest);
        verify(repository, atLeastOnce()).save(any(MenuItem.class));
        Assertions.assertEquals(menuItem, result);
    }

    @Test
    void whenUpdateMenuItemAndFoundShouldReturnTheUpdatedMenuItem() {
        when(repository.findById(any(Integer.class))).thenReturn(Optional.of(menuItem));
        when(repository.save(any(MenuItem.class))).thenAnswer(invocation ->
                invocation.getArgument(0, MenuItem.class));

        MenuItem result = service.update(0, updateRequest);
        verify(repository, atLeastOnce()).save(any(MenuItem.class));
        Assertions.assertEquals(newMenuItem, result);
    }

    @Test
    void whenUpdateMenuItemAndNotFoundShouldThrowException() {
        when(repository.findById(any(Integer.class))).thenReturn(Optional.empty());
        Assertions.assertThrows(MenuItemDoesNotExistException.class, () -> service.findById(0));
    }
}
