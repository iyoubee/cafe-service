package id.ac.ui.cs.advprog.cafeservice.service.menu;

import id.ac.ui.cs.advprog.cafeservice.dto.MenuItemRequest;
import id.ac.ui.cs.advprog.cafeservice.dto.OrderDetailsData;
import id.ac.ui.cs.advprog.cafeservice.dto.OrderRequest;
import id.ac.ui.cs.advprog.cafeservice.exceptions.MenuItemDoesNotExistException;
import id.ac.ui.cs.advprog.cafeservice.exceptions.OrderDoesNotExistException;
import id.ac.ui.cs.advprog.cafeservice.model.menu.MenuItem;
import id.ac.ui.cs.advprog.cafeservice.model.order.Order;
import id.ac.ui.cs.advprog.cafeservice.model.order.OrderDetails;
import id.ac.ui.cs.advprog.cafeservice.repository.MenuItemRepository;
import id.ac.ui.cs.advprog.cafeservice.repository.OrderDetailsRepository;
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
import java.util.*;
import java.util.List;
import java.util.Optional;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @InjectMocks
    private OrderServiceImpl service;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderDetailsRepository orderDetailsRepository;

    @Mock
    private MenuItemRepository menuItemRepository;

    Order order;

    Order newOrder;

    OrderDetails newOrderDetails;

    OrderDetailsData newOrderDetailsData;

    MenuItem menuItem;

    OrderRequest orderRequest;

    @BeforeEach
    void setUp() {

        menuItem = MenuItem.builder()
                .id("7dd3fd7a-4952-4eb2-8ba0-bbe1767b4a10")
                .name("Indomie")
                .price(10000)
                .stock(4)
                .build();

        newOrderDetailsData = new OrderDetailsData();
        newOrderDetailsData.setMenuItemId(menuItem.getId());
        newOrderDetailsData.setQuantity(0);
        newOrderDetailsData.setStatus("Approved");

        order = Order.builder()
                .id(287952)
                .session(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
                .orderDetailsList(Arrays.asList(
                        OrderDetails.builder()
                                .menuItem(menuItem)
                                .quantity(1)
                                .status("Approved")
                                .totalPrice(10000)
                                .build()))
                .build();

        newOrder = Order.builder()
                .id(287952)
                .session(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
                .orderDetailsList(Arrays.asList(
                        OrderDetails.builder()
                                .id(287952)
                                .order(order)
                                .menuItem(menuItem)
                                .quantity(1)
                                .status("Cancelled")
                                .totalPrice(10000)
                                .build()))
                .build();

        orderRequest = OrderRequest.builder()
                .session(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
                .orderDetailsData(Arrays.asList(newOrderDetailsData))
                .build();

        newOrderDetails = OrderDetails.builder()
                .id(287952)
                .order(order)
                .menuItem(menuItem)
                .quantity(1)
                .totalPrice(10000)
                .status("Cancelled")
                .build();
    }

    @Test
    void testHashCode() {
        OrderDetails orderDetails1 = OrderDetails.builder()
                .id(1)
                .order(new Order())
                .menuItem(new MenuItem())
                .quantity(2)
                .totalPrice(20)
                .status("pending")
                .build();

        OrderDetails orderDetails2 = OrderDetails.builder()
                .id(2)
                .order(new Order())
                .menuItem(new MenuItem())
                .quantity(3)
                .totalPrice(30)
                .status("completed")
                .build();

        assertNotEquals(orderDetails1.hashCode(), orderDetails2.hashCode());
    }

    @Test
    void testEquals() {
        MenuItem menuItem = new MenuItem();
        Order order = new Order();
        OrderDetails orderDetails1 = OrderDetails.builder()
                .id(1)
                .order(order)
                .menuItem(menuItem)
                .quantity(2)
                .totalPrice(20)
                .status("pending")
                .build();

        OrderDetails orderDetails2 = OrderDetails.builder()
                .id(1)
                .order(order)
                .menuItem(menuItem)
                .quantity(2)
                .totalPrice(20)
                .status("pending")
                .build();

        assertEquals(orderDetails1, orderDetails2);
    }

    @Test
    void testToString() {
        Order order = new Order();
        MenuItem menuItem = new MenuItem();
        OrderDetails orderDetails = OrderDetails.builder()
                .id(1)
                .order(order)
                .menuItem(menuItem)
                .quantity(2)
                .totalPrice(20)
                .status("pending")
                .build();

        String expectedString = "OrderDetails(id=1, order=" + order.toString() + ", menuItem=" + menuItem.toString()
                + ", quantity=2, totalPrice=20, status=pending)";
        assertEquals(expectedString, orderDetails.toString());
    }

    @Test
    void testFindAll() {
        List<Order> orders = List.of(
                Order.builder().id(1).session(UUID.fromString("123e4567-e89b-12d3-a456-426614174000")).build(),
                Order.builder().id(2).session(UUID.fromString("123e4567-e89b-12d3-a456-426614174001")).build());
        when(orderRepository.findAll()).thenReturn(orders);

        OrderServiceImpl orderService = new OrderServiceImpl(orderRepository, orderDetailsRepository,
                menuItemRepository);
        List<Order> foundOrders = orderService.findAll();

        assertEquals(2, foundOrders.size());
        assertEquals(orders, foundOrders);
        verify(orderRepository, times(1)).findAll();
    }

    @Test
    void testIsOrderDoesNotExist() {
        when(orderRepository.findById(1)).thenReturn(Optional.empty());
        when(orderRepository.findById(2)).thenReturn(Optional.of(Order.builder().id(2).build()));

        OrderServiceImpl orderService = new OrderServiceImpl(orderRepository, orderDetailsRepository, menuItemRepository);
        assertTrue(orderService.isOrderDoesNotExist(1));
        assertFalse(orderService.isOrderDoesNotExist(2));

        verify(orderRepository, times(2)).findById(anyInt());
    }

    @Test
    void whenCreateOrderButMenuItemNotFoundShouldThrowException() {
        when(menuItemRepository.findById(any(String.class))).thenReturn(Optional.empty());

        assertThrows(MenuItemDoesNotExistException.class, () -> {
            service.create(orderRequest);
        });
    }

    @Test
    void whenUpdateOrderAndFoundShouldReturnTheUpdatedMenuItem() {
        when(orderRepository.findById(any(Integer.class))).thenReturn(Optional.of(order));
        when(menuItemRepository.findById(any(String.class))).thenReturn(Optional.of(menuItem));
        when(orderDetailsRepository.save(any(OrderDetails.class))).thenReturn(newOrderDetails);
        Order result = service.update(287952, orderRequest);
        Assertions.assertEquals(newOrder, result);
    }


    @Test
    void whenUpdateOrderAndNotFoundShouldThrowException() {
        when(orderRepository.findById(any(Integer.class))).thenReturn(Optional.empty());
        Assertions.assertThrows(OrderDoesNotExistException.class, () -> service.update(287952, orderRequest));
    }
}
