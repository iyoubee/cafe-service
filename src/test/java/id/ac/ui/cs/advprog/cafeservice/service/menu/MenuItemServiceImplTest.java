package id.ac.ui.cs.advprog.cafeservice.service.menu;

import id.ac.ui.cs.advprog.cafeservice.dto.MenuItemRequest;
import id.ac.ui.cs.advprog.cafeservice.exceptions.MenuItemDoesNotExistException;
import id.ac.ui.cs.advprog.cafeservice.model.menu.MenuItem;
import id.ac.ui.cs.advprog.cafeservice.model.order.OrderDetails;
import id.ac.ui.cs.advprog.cafeservice.repository.MenuItemRepository;
import id.ac.ui.cs.advprog.cafeservice.repository.OrderDetailsRepository;
import id.ac.ui.cs.advprog.cafeservice.service.MenuItemServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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
        assertEquals(allMenuItem, result);

        result = service.findAll("otherQuery");
        verify(repository, never()).findByStockGreaterThan(anyInt());
        assertEquals(allMenuItem, result);
    }

    @Test
    void testWhenFindAllMenuItemWithQueryShouldReturnListOfAvailableMenuItem() {
        List<MenuItem> availableMenuItems = List.of(menuItem);

        when(repository.findByStockGreaterThan(anyInt())).thenReturn(availableMenuItems);

        List<MenuItem> result = service.findAll("available");
        verify(repository, atLeastOnce()).findByStockGreaterThan(0);
        assertEquals(availableMenuItems, result);
    }

    @Test
    void testWhenFindByIdAndFoundShouldReturnMenuItem(){
        when(repository.findById(any(String.class))).thenReturn(Optional.of(menuItem));

        MenuItem result = service.findById("7dd3fd7a-4952-4eb2-8ba0-bbe1767b4a10");
        verify(repository, atLeastOnce()).findById(any(String.class));
        assertEquals(menuItem, result);
    }

    @Test
    void testWhenFindByIdAndNotFoundShouldThrowException() {
        when(repository.findById(any(String.class))).thenReturn(Optional.empty());

        Assertions.assertThrows(MenuItemDoesNotExistException.class, () -> service.findById("7dd3fd7a-4952-4eb2-8ba0-bbe1767b4a11"));
    }

    @Test
    void testWhenCreateMenuItemShouldReturnTheCreatedMenuItem() {

        when(repository.save(any(MenuItem.class))).thenReturn(menuItem);
        MenuItem result = service.create(createRequest);
        verify(repository, atLeastOnce()).save(any(MenuItem.class));
        assertEquals(menuItem.getName(), result.getName());
        assertEquals(menuItem.getPrice(), result.getPrice());
        assertEquals(menuItem.getStock(), result.getStock());
    }

    @Test
    void testWhenUpdateMenuItemAndFoundShouldReturnTheUpdatedMenuItem() {
        when(repository.findById(any(String.class))).thenReturn(Optional.of(newMenuItem));
        when(repository.save(any(MenuItem.class))).thenAnswer(invocation ->
                invocation.getArgument(0, MenuItem.class));

        MenuItem result = service.update("7dd3fd7a-4952-4eb2-8ba0-bbe1767b4a10", updateRequest);
        verify(repository, atLeastOnce()).save(any(MenuItem.class));
        assertEquals(newMenuItem.getName(), result.getName());
        assertEquals(newMenuItem.getPrice(), result.getPrice());
        assertEquals(newMenuItem.getStock(), result.getStock());
    }

    @Test
    void testWhenUpdateMenuItemAndNotFoundShouldThrowException() {
        when(repository.findById(any(String.class))).thenReturn(Optional.empty());
        Assertions.assertThrows(MenuItemDoesNotExistException.class, () -> service.update("f20a0089-a4d6-49d7-8be8-9cdc81bd7341", updateRequest));
    }

    @Test
    void testWhenDeleteMenuItemAndFoundShouldCallDeleteByIdOnRepo() {
        UUID menuItemId = UUID.fromString("7dd3fd7a-4952-4eb2-8ba0-bbe1767b4a10");
        List<OrderDetails> orderDetailsList = new ArrayList<>();
        OrderDetails orderDetails1 = OrderDetails.builder().status("Menunggu Konfirmasi").build();
        OrderDetails orderDetails2 = OrderDetails.builder().status("Selesai").build();
        orderDetailsList.add(orderDetails1);
        orderDetailsList.add(orderDetails2);

        when(repository.findById(any(String.class))).thenReturn(Optional.of(menuItem));
        when(orderDetailsRepository.getByMenuItem(menuItemId.toString())).thenReturn(orderDetailsList);

        service.delete(menuItemId.toString());
        verify(repository, atLeastOnce()).deleteById(menuItemId.toString());

        for (OrderDetails orderDetails : orderDetailsList) {
            assertNull(orderDetails.getMenuItem());

            if (!orderDetails.getStatus().equals("Selesai")) {
                assertEquals("Dibatalkan", orderDetails.getStatus());
            }

            verify(orderDetailsRepository).save(orderDetails);
        }
    }

    @Test
    void testWhenDeleteMenuItemAndNotFoundShouldThrowException() {
        when(repository.findById(any(String.class))).thenReturn(Optional.empty());
        Assertions.assertThrows(MenuItemDoesNotExistException.class, () -> service.delete("f20a0089-a4d6-49d7-8be8-9cdc81bd7341"));
    }
}
