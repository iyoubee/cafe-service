package id.ac.ui.cs.advprog.cafeservice.service.menu;

import id.ac.ui.cs.advprog.cafeservice.dto.MenuItemRequest;
import id.ac.ui.cs.advprog.cafeservice.dto.OrderRequest;
import id.ac.ui.cs.advprog.cafeservice.exceptions.MenuItemDoesNotExistException;
import id.ac.ui.cs.advprog.cafeservice.exceptions.OrderDoesNotExistException;
import id.ac.ui.cs.advprog.cafeservice.model.menu.MenuItem;
import id.ac.ui.cs.advprog.cafeservice.model.order.Order;
import id.ac.ui.cs.advprog.cafeservice.model.order.OrderDetails;
import id.ac.ui.cs.advprog.cafeservice.repository.MenuItemRepository;
import id.ac.ui.cs.advprog.cafeservice.repository.OrderRepository;
import id.ac.ui.cs.advprog.cafeservice.service.MenuItemServiceImpl;
import id.ac.ui.cs.advprog.cafeservice.service.OrderServiceImpl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @InjectMocks
    private OrderServiceImpl service;

    @Mock
    private OrderRepository repository;

    Order order;

    Order newOrder;

    MenuItem menuItem;

    OrderRequest updateRequest;

    @BeforeEach
    void setUp() {

        menuItem = MenuItem.builder()
            .name("Indomie")
            .price(10000)
            .stock(4)
            .build();

        order = Order.builder()
        .id(287952)
        .pc(12045)
        .orderDetailsList(Arrays.asList(
            OrderDetails.builder()
                .menuItem(menuItem)
                .quantity(1)
                .status("Approved")
                .totalPrice(10000)
                .build()
        ))
        .build();

        
        newOrder = Order.builder()
        .id(287952)
        .pc(1204)
        .orderDetailsList(Arrays.asList(
            OrderDetails.builder()
                .menuItem(menuItem)
                .quantity(1)
                .status("Cancelled")
                .totalPrice(10000)
                .build()
        ))
        .build();

        updateRequest = OrderRequest.builder()
        .pc(1204)
        .orderDetailsList(Arrays.asList(
            OrderDetails.builder()
                .menuItem(menuItem)
                .quantity(1)
                .status("Cancelled")
                .totalPrice(10000)
                .build()
        ))
        .build();
    }

    @Test
    void whenUpdateOrderAndFoundShouldReturnTheUpdatedMenuItem() {
        when(repository.findById(any(Integer.class))).thenReturn(Optional.of(order));
        when(repository.save(any(Order.class))).thenAnswer(invocation ->
                invocation.getArgument(0, Order.class));

        Order result = service.update(287952, updateRequest);
        verify(repository, atLeastOnce()).save(any(Order.class));
        Assertions.assertEquals(newOrder, result);
    }

    @Test
    void whenUpdateOrderAndNotFoundShouldThrowException() {
        when(repository.findById(any(Integer.class))).thenReturn(Optional.empty());
        Assertions.assertThrows(OrderDoesNotExistException.class, () -> service.update(287952, updateRequest));
    }
}
