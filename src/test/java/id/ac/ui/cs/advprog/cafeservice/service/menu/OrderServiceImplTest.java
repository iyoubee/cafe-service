package id.ac.ui.cs.advprog.cafeservice.service.menu;

import id.ac.ui.cs.advprog.cafeservice.dto.MenuItemRequest;
import id.ac.ui.cs.advprog.cafeservice.dto.OrderDetailsData;
import id.ac.ui.cs.advprog.cafeservice.dto.OrderRequest;
import id.ac.ui.cs.advprog.cafeservice.exceptions.InvalidJSONException;
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

import org.aspectj.weaver.ast.Or;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

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

    Order createdOrder;

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

        createdOrder = Order.builder()
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
    void whenFindByIdWithExistingOrderShouldReturnOrder() {
        Integer id = 1;
        Order expectedOrder = new Order(id, UUID.randomUUID(), new ArrayList<>());
        when(orderRepository.findById(id)).thenReturn(Optional.of(expectedOrder));

        Order result = service.findById(id);

        // Assert
        assertEquals(expectedOrder, result);
        verify(orderRepository, times(1)).findById(id);
    }

    @Test
    void whenFindByIdWithNonExistingOrderShouldThrowOrderDoesNotExistException() {
        Integer id = 1;
        when(orderRepository.findById(id)).thenReturn(Optional.empty());
        OrderDoesNotExistException exception = assertThrows(OrderDoesNotExistException.class, () -> {
            service.findById(id);
        });
        assertEquals("Order with id " + id + " does not exist", exception.getMessage());
        verify(orderRepository, times(1)).findById(id);
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
    void whenCreateOrderShouldReturnTheCreatedMenuItem() {
        when(menuItemRepository.findById(any(String.class))).thenReturn(Optional.of(menuItem));
        when(orderDetailsRepository.save(any(OrderDetails.class))).thenReturn(newOrderDetails);
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        Order result = service.create(orderRequest);

        verify(orderRepository, atLeastOnce()).save(any(Order.class));
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
    void whenUpdateOrderAndOrderDetailsExistsShouldUpdateOrderDetailsAndReturnUpdatedOrder() {
        when(orderRepository.findById(any(Integer.class))).thenReturn(Optional.of(order));
        when(menuItemRepository.findById(any(String.class))).thenReturn(Optional.of(menuItem));
        OrderDetails existingOrderDetails = OrderDetails.builder()
                .id(1)
                .order(order)
                .menuItem(menuItem)
                .quantity(2)
                .totalPrice(20000)
                .build();
        when(orderDetailsRepository.findByOrderIdAndMenuItemId(any(Integer.class), any(String.class)))
                .thenReturn(Optional.of(existingOrderDetails));
        when(orderDetailsRepository.save(any(OrderDetails.class))).thenReturn(newOrderDetails);
        Order result = service.update(287952, orderRequest);
        verify(orderDetailsRepository, times(1)).deleteAll(anyList());
        verify(orderDetailsRepository, times(1)).save(any(OrderDetails.class));
        verify(orderRepository, times(1)).findById(any(Integer.class));
        verify(menuItemRepository, times(1)).findById(any(String.class));
        Assertions.assertEquals(newOrder, result);
    }

    @Test
    void whenUpdateOrderButMenuItemNotFoundShouldThrowException() {
        when(orderRepository.findById(any(Integer.class))).thenReturn(Optional.of(order));
        when(menuItemRepository.findById(any(String.class))).thenReturn(Optional.empty());
        assertThrows(MenuItemDoesNotExistException.class, () -> {
            service.update(287952,orderRequest);
        });
    }

    @Test
    void whenUpdateOrderAndNotFoundShouldThrowException() {
        when(orderRepository.findById(any(Integer.class))).thenReturn(Optional.empty());
        Assertions.assertThrows(OrderDoesNotExistException.class, () -> service.update(287952, orderRequest));
    }

    @Test
    void whenDeleteOrderAndFoundShouldDeleteOrder() {
        int orderId = 1;
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(Order.builder().id(orderId).build()));
        service.delete(orderId);
        verify(orderRepository, times(1)).deleteById(orderId);
    }

    @Test
    void whenDeleteOrderAndNotFoundShouldThrowException() {
        int orderId = 1;
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());
        assertThrows(OrderDoesNotExistException.class, () -> service.delete(orderId));
    }

    @Test
    void testFindBySession() {
        UUID session = UUID.randomUUID();
        List<Order> orders = Arrays.asList(
                Order.builder().id(1).session(session).build(),
                Order.builder().id(2).session(session).build());
        when(orderRepository.findBySession(session)).thenReturn(Optional.of(orders));

        OrderServiceImpl orderService = new OrderServiceImpl(orderRepository, orderDetailsRepository, menuItemRepository);
        List<Order> foundOrders = orderService.findBySession(session);

        assertEquals(2, foundOrders.size());
        assertEquals(orders, foundOrders);
        verify(orderRepository, times(1)).findBySession(session);
    }

    @Test
    void testAddToBill() throws JSONException {
        // Create an instance of OrderDetails with some test data


        // Set up a mock RestTemplate and a mock response from the server
        RestTemplate restTemplateMock = Mockito.mock(RestTemplate.class);
//        ResponseEntity<String> mockResponse = new ResponseEntity<>("{\"status\": \"success\"}", HttpStatus.OK);
//        Mockito.when(restTemplateMock.postForObject(Mockito.anyString(), Mockito.any(HttpEntity.class), Mockito.any(Class.class)))
//                .thenReturn(mockResponse);

        // Call the addToBill method with the mock RestTemplate and verify that it sends the expected request
        int id = 2;
        String expectedUrl = "http://34.142.223.187/api/v1/invoices/" + id + "/bills";
        JSONObject expectedRequestBody = new JSONObject();
        expectedRequestBody.put("name", menuItem.getName());
        expectedRequestBody.put("price", menuItem.getPrice());
        expectedRequestBody.put("quantity", newOrderDetails.getQuantity());
        expectedRequestBody.put("subTotal", (long) menuItem.getPrice() * newOrderDetails.getQuantity());
        expectedRequestBody.put("invoiceId", id);

        HttpHeaders expectedHeaders = new HttpHeaders();
        expectedHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> expectedEntity = new HttpEntity<>(expectedRequestBody.toString(), expectedHeaders);
        OrderServiceImpl orderService = new OrderServiceImpl(orderRepository, orderDetailsRepository, menuItemRepository);
        orderService.setRestTemplate(restTemplateMock);

        orderService.addToBill(newOrderDetails);

        Mockito.verify(restTemplateMock).postForObject(expectedUrl, expectedEntity, String.class);
    }

    @Test
    void whenJSONRequestInvalidShouldThrowException() {
        String expectedMessage = "Invalid request body";
        InvalidJSONException exception = new InvalidJSONException();
        assertEquals(expectedMessage, exception.getMessage());
    }

}
