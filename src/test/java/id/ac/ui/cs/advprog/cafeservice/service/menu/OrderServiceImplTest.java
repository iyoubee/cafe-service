package id.ac.ui.cs.advprog.cafeservice.service.menu;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.cafeservice.Util;
import id.ac.ui.cs.advprog.cafeservice.dto.MenuItemRequest;
import id.ac.ui.cs.advprog.cafeservice.dto.OrderDetailsData;
import id.ac.ui.cs.advprog.cafeservice.dto.OrderRequest;
import id.ac.ui.cs.advprog.cafeservice.exceptions.*;
import id.ac.ui.cs.advprog.cafeservice.model.menu.MenuItem;
import id.ac.ui.cs.advprog.cafeservice.model.order.Order;
import id.ac.ui.cs.advprog.cafeservice.model.order.OrderDetails;
import id.ac.ui.cs.advprog.cafeservice.pattern.statusStrategy.DoneStatus;
import id.ac.ui.cs.advprog.cafeservice.pattern.statusStrategy.StatusStrategy;
import id.ac.ui.cs.advprog.cafeservice.repository.MenuItemRepository;
import id.ac.ui.cs.advprog.cafeservice.repository.OrderDetailsRepository;
import id.ac.ui.cs.advprog.cafeservice.repository.OrderRepository;
import id.ac.ui.cs.advprog.cafeservice.service.MenuItemService;
import id.ac.ui.cs.advprog.cafeservice.service.MenuItemServiceImpl;
import id.ac.ui.cs.advprog.cafeservice.service.OrderService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

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

    @Mock
    private MenuItemService menuItemService;

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
                                .quantity(20)
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
                .status("Cancelled")
                .totalPrice(10000)
                .build();
    }

    @Test
    void testHashCode() {
        OrderDetails orderDetails1 = OrderDetails.builder()
                .id(1)
                .order(new Order())
                .menuItem(new MenuItem())
                .quantity(2)
                .status("pending")
                .totalPrice(10000)
                .build();

        OrderDetails orderDetails2 = OrderDetails.builder()
                .id(2)
                .order(new Order())
                .menuItem(new MenuItem())
                .quantity(3)
                .status("completed")
                .totalPrice(10000)
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
                .status("pending")
                .totalPrice(20)
                .build();

        OrderDetails orderDetails2 = OrderDetails.builder()
                .id(1)
                .order(order)
                .menuItem(menuItem)
                .quantity(2)
                .status("pending")
                .totalPrice(20)
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
                .status("Menunggu Konfirmasi")
                .totalPrice(20)
                .build();

        String expectedString = "OrderDetails(id=1, order=" + order.toString() + ", menuItem=" + menuItem.toString()
                + ", quantity=2, status=Menunggu Konfirmasi, totalPrice=20)";
        assertEquals(expectedString, orderDetails.toString());
    }

    @Test
    void testFindAll() {
        List<Order> orders = List.of(
                Order.builder().id(1).session(UUID.fromString("123e4567-e89b-12d3-a456-426614174000")).build(),
                Order.builder().id(2).session(UUID.fromString("123e4567-e89b-12d3-a456-426614174001")).build());
        when(orderRepository.findAll()).thenReturn(orders);
        OrderServiceImpl orderService = new OrderServiceImpl(orderRepository, orderDetailsRepository, menuItemService,
                menuItemRepository);
        List<Order> foundOrders = orderService.findAll();

        assertEquals(2, foundOrders.size());
        assertEquals(orders, foundOrders);
        verify(orderRepository, times(1)).findAll();
    }

    @Test
    void testFindOrderByPaginationOrderLessThanSixteen() {
        List<Order> orders = Arrays.asList(
                Order.builder().id(1).session(UUID.fromString("123e4567-e89b-12d3-a456-426614174000")).build(),
                Order.builder().id(2).session(UUID.fromString("123e4567-e89b-12d3-a456-426614174001")).build(),
                Order.builder().id(3).session(UUID.fromString("123e4567-e89b-12d3-a456-426614174002")).build(),
                Order.builder().id(4).session(UUID.fromString("123e4567-e89b-12d3-a456-426614174003")).build());

        when(orderRepository.findAll()).thenReturn(orders);

        OrderServiceImpl orderService = new OrderServiceImpl(orderRepository, orderDetailsRepository, menuItemService,
                menuItemRepository);

        List<Order> foundOrdersPage1 = orderService.findByPagination(1);
        int expectedPageSize = 4;
        assertEquals(expectedPageSize, foundOrdersPage1.size());
        assertEquals(orders.subList(0, expectedPageSize), foundOrdersPage1);
        verify(orderRepository, times(1)).findAll();
    }

    @Test
    void testFindOrderByPaginationOrderEqualToSixteen() {
        List<Order> orders = new ArrayList<>();

        for (int i = 1; i <= 16; i++) {
            String uuidString = "123e4567-e89b-12d3-a456-4266141740" + String.format("%02d", i);
            System.out.println(uuidString);
            UUID sessionUUID = UUID.fromString(uuidString);
            Order order = Order.builder()
                    .id(i)
                    .session(sessionUUID)
                    .build();
            orders.add(order);
        }

        when(orderRepository.findAll()).thenReturn(orders);

        OrderServiceImpl orderService = new OrderServiceImpl(orderRepository, orderDetailsRepository, menuItemService,
                menuItemRepository);

        List<Order> foundOrdersPage1 = orderService.findByPagination(1);
        int expectedPageSize = 16;
        assertEquals(expectedPageSize, foundOrdersPage1.size());
        assertEquals(orders.subList(0, expectedPageSize), foundOrdersPage1);
        verify(orderRepository, times(1)).findAll();
    }

    @Test
    void testFindOrderByPaginationOrderMoreThanSixteen() {
        List<Order> orders = new ArrayList<>();

        for (int i = 1; i <= 18; i++) {
            String uuidString = "123e4567-e89b-12d3-a456-4266141740" + String.format("%02d", i);
            UUID sessionUUID = UUID.fromString(uuidString);
            Order order = Order.builder()
                    .id(i)
                    .session(sessionUUID)
                    .build();
            orders.add(order);
        }

        when(orderRepository.findAll()).thenReturn(orders);

        OrderServiceImpl orderService = new OrderServiceImpl(orderRepository, orderDetailsRepository, menuItemService,
                menuItemRepository);

        List<Order> foundOrdersPage1 = orderService.findByPagination(1);
        int expectedPageSize = 16;
        assertEquals(expectedPageSize, foundOrdersPage1.size());
        assertEquals(orders.subList(0, expectedPageSize), foundOrdersPage1);
        verify(orderRepository, times(1)).findAll();
    }

    @Test
    void testFindOrderByPaginationOrderMoreThanSixteenPageTwo() {
        List<Order> orders = new ArrayList<>();

        for (int i = 1; i <= 18; i++) {
            String uuidString = "123e4567-e89b-12d3-a456-4266141740" + String.format("%02d", i);
            UUID sessionUUID = UUID.fromString(uuidString);
            Order order = Order.builder()
                    .id(i)
                    .session(sessionUUID)
                    .build();
            orders.add(order);
        }

        when(orderRepository.findAll()).thenReturn(orders);

        OrderServiceImpl orderService = new OrderServiceImpl(orderRepository, orderDetailsRepository, menuItemService,
                menuItemRepository);

        int expectedPageSize = 2;
        List<Order> foundOrdersPage2 = orderService.findByPagination(2);
        assertEquals(expectedPageSize, foundOrdersPage2.size());
        assertEquals(orders.subList(16, 18), foundOrdersPage2);
        verify(orderRepository, times(1)).findAll();

    }

    @Test
    void whenFindByIdWithExistingOrderShouldReturnOrder() {
        Integer id = 1;
        Integer totalPrice = 10000;
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

        OrderServiceImpl orderService = new OrderServiceImpl(orderRepository, orderDetailsRepository, menuItemService, menuItemRepository);
        assertTrue(orderService.isOrderDoesNotExist(1));
        assertFalse(orderService.isOrderDoesNotExist(2));

        verify(orderRepository, times(2)).findById(anyInt());
    }

    @Test
    void whenCreateOrderShouldReturnTheCreatedMenuItem() {
        when(menuItemRepository.findById(any(String.class))).thenReturn(Optional.of(menuItem));
        when(orderDetailsRepository.save(any(OrderDetails.class))).thenReturn(newOrderDetails);
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        Order result = service.create(orderRequest, null);

        verify(orderRepository, atLeastOnce()).save(any(Order.class));
    }

    @Test
    void whenCreateOrderButMenuItemNotFoundShouldThrowException() {
        when(menuItemRepository.findById(any(String.class))).thenReturn(Optional.empty());

        assertThrows(MenuItemDoesNotExistException.class, () -> {
            service.create(orderRequest, null);
        });
    }

    @Test
    void whenCreateOrderAndMenuItemOutOfStockShouldThrowMenuItemOutOfStockException() {
        when(menuItemRepository.findById(any(String.class))).thenReturn(Optional.of(menuItem));
        List<OrderDetailsData> orderDetailsDataList = new ArrayList<>();
        OrderDetailsData orderDetailsData = new OrderDetailsData();
        orderDetailsData.setMenuItemId("7dd3fd7a-4952-4eb2-8ba0-bbe1767b4a10");
        orderDetailsData.setQuantity(1000);
        orderDetailsDataList.add(orderDetailsData);
        OrderRequest orderRequest = OrderRequest.builder()
                .session(UUID.randomUUID())
                .orderDetailsData(orderDetailsDataList)
                .build();
        assertThrows(MenuItemOutOfStockException.class, () -> service.create(orderRequest, null));
    }

    @Test
    void whenCreateOrderFromAnotherSquadTheTotalPriceShouldBeZero() {
        MenuItem item = MenuItem.builder()
                .id("1")
                .price(5000)
                .stock(10)
                .build();

        Order order1 = Order.builder()
                .session(UUID.randomUUID())
                .build();
        OrderDetails orderDetails = OrderDetails.builder()
                .order(order)
                .menuItem(item)
                .quantity(1)
                .status("Menunggu konfirmasi")
                .totalPrice(0)
                .build();
        order.setOrderDetailsList(Collections.singletonList(orderDetails));

        when(menuItemRepository.findById(any(String.class))).thenReturn(Optional.of(item));
        when(orderDetailsRepository.save(any(OrderDetails.class))).thenReturn(orderDetails);
        when(orderRepository.save(any(Order.class))).thenReturn(order1);

        Order result = service.create(orderRequest, "warnet");

        verify(orderRepository, atLeastOnce()).save(any(Order.class));

    }

    @Test
    void whenCancleOrder() {
        // Set up mock data
        OrderDetails orderDetails = OrderDetails.builder()
                .id(2)
                .quantity(1)
                .menuItem(menuItem)
                .status("Menunggu Konfirmasi")
                .totalPrice(10000)
                .build();

        // Set up mock repository
        when(orderDetailsRepository.findById(any(Integer.class))).thenReturn(Optional.of(orderDetails));

        OrderDetails cancel = service.updateOrderDetailStatus(2, "cancel");
        assertEquals(orderDetails, cancel);
    }

    @Test
    void whenPrepareOrder() {
        // Set up mock data
        OrderDetails orderDetails = OrderDetails.builder()
                .id(2)
                .quantity(1)
                .menuItem(menuItem)
                .status("Menunggu Konfirmasi")
                .totalPrice(10000)
                .build();

        // Set up mock repository
        when(orderDetailsRepository.findById(any(Integer.class))).thenReturn(Optional.of(orderDetails));

        OrderDetails prepare = service.updateOrderDetailStatus(2, "prepare");
        assertEquals(orderDetails, prepare);
    }

    @Test
    void whenDeliverOrder() {
        // Set up mock data
        OrderDetails orderDetails = OrderDetails.builder()
                .id(2)
                .quantity(1)
                .menuItem(menuItem)
                .status("Menunggu Konfirmasi")
                .totalPrice(10000)
                .build();

        // Set up mock repository
        when(orderDetailsRepository.findById(any(Integer.class))).thenReturn(Optional.of(orderDetails));

        OrderDetails deliver = service.updateOrderDetailStatus(2, "deliver");
        assertEquals(orderDetails, deliver);
    }

    @Test
    void whenNegativeCancleOrder() {
        // Set up mock data
        OrderDetails orderDetails = OrderDetails.builder()
                .id(2)
                .quantity(1)
                .menuItem(menuItem)
                .status("Sedang Disiapkan")
                .totalPrice(10000)
                .build();

        // Set up mock repository
        when(orderDetailsRepository.findById(any(Integer.class))).thenReturn(Optional.of(orderDetails));

        try {
            service.updateOrderDetailStatus(2, "cancel");
        } catch (OrderDetailStatusInvalid e) {
            String expectedMessage = "Order Detail status with id 2 invalid";
            String actualMessage = e.getMessage();

            assertTrue(actualMessage.contains(expectedMessage));
        }
    }

    @Test
    void whenUpdateOrderAndStatusAlreadyDoneOrCanceled() {
        // Set up mock data
        OrderDetails orderDetails = OrderDetails.builder()
                .id(1)
                .quantity(1)
                .menuItem(menuItem)
                .status("Selesai")
                .totalPrice(10000)
                .build();
        List<OrderDetails> orderDetailsList = List.of(orderDetails);
        Order order = Order.builder()
                .id(1)
                .session(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
                .orderDetailsList(orderDetailsList)
                .build();

        // Set up mock repository
        when(orderDetailsRepository.findById(any(Integer.class))).thenReturn(Optional.of(orderDetails));

        try {
            service.updateOrderDetailStatus(1, "deliver");
        } catch (OrderDetailStatusInvalid e) {
            String expectedMessage = "Order Detail status with id 1 invalid";
            String actualMessage = e.getMessage();

            assertTrue(actualMessage.contains(expectedMessage));
        }
    }

    @Test
    void whenUpdateBadRequest() {
        // Set up mock data
        OrderDetails orderDetails = OrderDetails.builder()
                .id(1)
                .quantity(1)
                .menuItem(menuItem)
                .status("Menunggu Konfirmasi")
                .totalPrice(10000)
                .build();
        List<OrderDetails> orderDetailsList = List.of(orderDetails);
        Order order = Order.builder()
                .id(1)
                .session(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
                .orderDetailsList(orderDetailsList)
                .build();

        // Set up mock repository
        when(orderDetailsRepository.findById(any(Integer.class))).thenReturn(Optional.of(orderDetails));

        try {
            OrderDetails prepare = service.updateOrderDetailStatus(1, "abc");
        } catch (BadRequest e) {
            String expectedMessage = "400 Bad Request";
            String actualMessage = e.getMessage();

            assertTrue(actualMessage.contains(expectedMessage));
        }
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

        OrderServiceImpl orderService = new OrderServiceImpl(orderRepository, orderDetailsRepository, menuItemService,
                menuItemRepository);
        List<Order> foundOrders = orderService.findBySession(session);

        assertEquals(2, foundOrders.size());
        assertEquals(orders, foundOrders);
        verify(orderRepository, times(1)).findBySession(session);
    }

    @Test
    void whenFindBySessionNotExist() {
        UUID session = UUID.randomUUID();
        List<Order> emptyOrders = new ArrayList<>();
        when(orderRepository.findBySession(session)).thenReturn(Optional.of(emptyOrders));

        OrderServiceImpl orderService = new OrderServiceImpl(orderRepository, orderDetailsRepository, menuItemService,
                menuItemRepository);
        List<Order> foundOrders = orderService.findBySession(session);

        assertEquals(0, foundOrders.size());
        assertEquals(emptyOrders, foundOrders);
        verify(orderRepository, times(1)).findBySession(session);
    }

    @Test
    void testAddToBill() throws JSONException {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        UUID session = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

        JSONObject requestBody = new JSONObject();
        requestBody.put("name", menuItem.getName());
        requestBody.put("price", menuItem.getPrice());
        requestBody.put("quantity", newOrderDetails.getQuantity());
        requestBody.put("subTotal", (long) newOrderDetails.getTotalPrice());
        requestBody.put("sessionId", session);

        HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);
        String billUrl = "http://34.142.223.187/api/v1/bills";

        JSONObject expectedResponse = new JSONObject();
        expectedResponse.put("id", 1);
        expectedResponse.put("name", menuItem.getName());
        expectedResponse.put("price", menuItem.getPrice());
        expectedResponse.put("quantity", newOrderDetails.getQuantity());
        expectedResponse.put("subTotal", (long) newOrderDetails.getTotalPrice());

        RestTemplate restTemplate = mock(RestTemplate.class);
        OrderServiceImpl orderService = new OrderServiceImpl(orderRepository, orderDetailsRepository, menuItemService,
                menuItemRepository);
        orderService.setRestTemplate(restTemplate);

        when(restTemplate.postForObject((billUrl), (entity), (String.class))).thenReturn(expectedResponse.toString());

        orderService.addToBill(newOrderDetails);

        verify(restTemplate).postForObject(billUrl, entity, String.class);
    }

    @Test
    void whenJSONRequestInvalidShouldThrowException() {
        String expectedMessage = "Invalid request body";
        InvalidJSONException exception = new InvalidJSONException();
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void testUUIDNotFoundException() {
        String expectedMessage = "The UUID is not found";
        UUIDNotFoundException exception = new UUIDNotFoundException();
        assertEquals(exception.getMessage(), expectedMessage);
    }

    @Test
    void testOrderDetailsDoesNotExistException() {
        int orderId = 1;
        String expectedMessage = "Order Detail with id " + orderId + " does not exist";
        OrderDetailDoesNotExistException exception = new OrderDetailDoesNotExistException(orderId);
        assertEquals(exception.getMessage(), expectedMessage);
    }

    @Test
    void whenDoneOrder() {
        // Set up mock data
        order = Order.builder()
                .id(287952)
                .session(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
                .build();

        OrderDetails orderDetails = OrderDetails.builder()
                .id(2)
                .quantity(1)
                .menuItem(menuItem)
                .status("Menunggu Konfirmasi")
                .totalPrice(10000)
                .order(order)
                .build();

        // Set up mock repository
        when(orderDetailsRepository.findById(any(Integer.class))).thenReturn(Optional.of(orderDetails));

        RestTemplate restTemplate = mock(RestTemplate.class);
        OrderServiceImpl orderService = new OrderServiceImpl(orderRepository, orderDetailsRepository, menuItemService,
                menuItemRepository);
        orderService.setRestTemplate(restTemplate);

        OrderDetails deliver = orderService.updateOrderDetailStatus(2, "done");
        assertEquals(orderDetails, deliver);
    }
}
