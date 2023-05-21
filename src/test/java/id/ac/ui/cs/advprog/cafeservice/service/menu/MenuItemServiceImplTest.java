package id.ac.ui.cs.advprog.cafeservice.service.menu;

import id.ac.ui.cs.advprog.cafeservice.dto.MenuItemRequest;
import id.ac.ui.cs.advprog.cafeservice.exceptions.MenuItemDoesNotExistException;
import id.ac.ui.cs.advprog.cafeservice.model.menu.MenuItem;
import id.ac.ui.cs.advprog.cafeservice.model.order.OrderDetails;
import id.ac.ui.cs.advprog.cafeservice.repository.MenuItemRepository;
import id.ac.ui.cs.advprog.cafeservice.repository.OrderDetailsRepository;
import id.ac.ui.cs.advprog.cafeservice.service.MenuItemService;
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
    @Mock
    private OrderDetailsRepository orderDetailsRepository;

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
    void testWhenFindAllMenuItemShouldReturnListOfMenuItem() {
        List<MenuItem> allMenuItem = List.of(menuItem);

        when(repository.findAll()).thenReturn(allMenuItem);

        List<MenuItem> result = service.findAll(null);
        verify(repository, atLeastOnce()).findAll();
        Assertions.assertEquals(allMenuItem, result);
    }

    @Test
    void testWhenFindAllMenuItemWithQueryShouldReturnListOfAvailableMenuItem() {
        List<MenuItem> availableMenuItems = List.of(menuItem);

        when(repository.findByStockGreaterThan(anyInt())).thenReturn(availableMenuItems);

        List<MenuItem> result = service.findAll("available");
        verify(repository, atLeastOnce()).findByStockGreaterThan(0);
        Assertions.assertEquals(availableMenuItems, result);
    }

    @Test
    void testWhenFindByIdAndFoundShouldReturnMenuItem(){
        when(repository.findById(any(String.class))).thenReturn(Optional.of(menuItem));

        MenuItem result = service.findById("7dd3fd7a-4952-4eb2-8ba0-bbe1767b4a10");
        verify(repository, atLeastOnce()).findById(any(String.class));
        Assertions.assertEquals(menuItem, result);
    }

    @Test
    void testWhenFindByIdAndNotFoundShouldThrowException() {
        when(repository.findById(any(String.class))).thenReturn(Optional.empty());

        Assertions.assertThrows(MenuItemDoesNotExistException.class, () -> service.findById("7dd3fd7a-4952-4eb2-8ba0-bbe1767b4a11"));
    }

    @Test
    void testWhenCreateMenuItemShouldReturnTheCreatedMenuItem() {
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
    void testWhenUpdateMenuItemAndFoundShouldReturnTheUpdatedMenuItem() {
        when(repository.findById(any(String.class))).thenReturn(Optional.of(menuItem));
        when(repository.save(any(MenuItem.class))).thenAnswer(invocation ->
                invocation.getArgument(0, MenuItem.class));

        MenuItem result = service.update("7dd3fd7a-4952-4eb2-8ba0-bbe1767b4a10", updateRequest);
        verify(repository, atLeastOnce()).save(any(MenuItem.class));
        Assertions.assertEquals(newMenuItem, result);
    }

    @Test
    void testWhenUpdateMenuItemAndNotFoundShouldThrowException() {
        when(repository.findById(any(String.class))).thenReturn(Optional.empty());
        Assertions.assertThrows(MenuItemDoesNotExistException.class, () -> service.update("f20a0089-a4d6-49d7-8be8-9cdc81bd7341", updateRequest));
    }

    @Test
    void testWhenDeleteMenuItemAndFoundShouldCallDeleteByIdOnRepo() {
        MenuItem newMenuItem = MenuItem.builder()
                .id("asd")
                .name("Es Teh")
                .price(2000)
                .stock(200)
                .build();
        OrderDetails orderDetails = OrderDetails.builder()
                .id(999)
                .menuItem(newMenuItem)
                .totalPrice(90)
                .quantity(5)
                .status("Sedang Disiapkan")
                .build();
        repository.save(newMenuItem);
        orderDetailsRepository.save(orderDetails);
        when(repository.findById(any(String.class))).thenReturn(Optional.of(menuItem));
        MenuItemService menuItemService = new MenuItemServiceImpl(repository, orderDetailsRepository);
        menuItemService.delete("asd");
        verify(repository, atLeastOnce()).deleteById(any(String.class));
    }

    @Test
    void testWhenDeleteMenuItemAndNotFoundShouldThrowException() {
        when(repository.findById(any(String.class))).thenReturn(Optional.empty());
        Assertions.assertThrows(MenuItemDoesNotExistException.class, () -> {
            service.delete("f20a0089-a4d6-49d7-8be8-9cdc81bd7341");
        });
    }
}
