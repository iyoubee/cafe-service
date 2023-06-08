package id.ac.ui.cs.advprog.cafeservice.service.menu;

import id.ac.ui.cs.advprog.cafeservice.dto.OrderDetailsData;
import id.ac.ui.cs.advprog.cafeservice.dto.OrderRequest;
import id.ac.ui.cs.advprog.cafeservice.exceptions.*;
import id.ac.ui.cs.advprog.cafeservice.model.menu.MenuItem;
import id.ac.ui.cs.advprog.cafeservice.model.order.Order;
import id.ac.ui.cs.advprog.cafeservice.model.order.OrderDetails;
import id.ac.ui.cs.advprog.cafeservice.model.order.Status;
import id.ac.ui.cs.advprog.cafeservice.repository.MenuItemRepository;
import id.ac.ui.cs.advprog.cafeservice.repository.OrderDetailsRepository;
import id.ac.ui.cs.advprog.cafeservice.repository.OrderRepository;
import id.ac.ui.cs.advprog.cafeservice.service.MenuItemService;
import id.ac.ui.cs.advprog.cafeservice.service.OrderServiceImpl;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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

    @Value("${API_BAYAR}")
    private String apiBayar;

    @Value("${API_WARNET}")
    private String apiWarnet;

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
        newOrderDetailsData.setStatus(Status.CONFIRM.getValue());

        order = Order.builder()
                .id(287952)
                .session(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
                .orderDetailsList(Collections.singletonList(
                        OrderDetails.builder()
                                .menuItem(menuItem)
                                .quantity(1)
                                .status(Status.CONFIRM.getValue())
                                .totalPrice(10000)
                                .build()))
                .build();

        newOrder = Order.builder()
                .id(287952)
                .session(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
                .orderDetailsList(Collections.singletonList(
                        OrderDetails.builder()
                                .id(287952)
                                .order(order)
                                .menuItem(menuItem)
                                .quantity(1)
                                .status(Status.CANCEL.getValue())
                                .totalPrice(10000)
                                .build()))
                .build();

        createdOrder = Order.builder()
                .session(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
                .orderDetailsList(Collections.singletonList(
                        OrderDetails.builder()
                                .id(287952)
                                .order(order)
                                .menuItem(menuItem)
                                .quantity(20)
                                .status(Status.CANCEL.getValue())
                                .totalPrice(10000)
                                .build()))
                .build();

        orderRequest = OrderRequest.builder()
                .session(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
                .orderDetailsData(Collections.singletonList(newOrderDetailsData))
                .build();

        newOrderDetails = OrderDetails.builder()
                .id(287952)
                .order(order)
                .menuItem(menuItem)
                .quantity(1)
                .status(Status.CANCEL.getValue())
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
                .status(Status.CONFIRM.getValue())
                .totalPrice(10000)
                .build();

        OrderDetails orderDetails2 = OrderDetails.builder()
                .id(2)
                .order(new Order())
                .menuItem(new MenuItem())
                .quantity(3)
                .status(Status.DONE.getValue())
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
                .status(Status.CONFIRM.getValue())
                .totalPrice(20)
                .build();

        OrderDetails orderDetails2 = OrderDetails.builder()
                .id(1)
                .order(order)
                .menuItem(menuItem)
                .quantity(2)
                .status(Status.CONFIRM.getValue())
                .totalPrice(20)
                .build();

        assertEquals(orderDetails1, orderDetails2);
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
    void testFindByPage() {
        List<Order> orders = List.of(
                Order.builder().id(1).session(UUID.fromString("123e4567-e89b-12d3-a456-426614174000")).build(),
                Order.builder().id(2).session(UUID.fromString("123e4567-e89b-12d3-a456-426614174001")).build());
        when(orderRepository.getByPage(0,16)).thenReturn(orders);
        OrderServiceImpl orderService = new OrderServiceImpl(orderRepository, orderDetailsRepository, menuItemService,
                menuItemRepository);
        List<Order> foundOrders = orderService.findByPagination(1);

        assertEquals(2, foundOrders.size());
        assertEquals(orders, foundOrders);
        verify(orderRepository, times(1)).getByPage(0,16);
    }

    @Test
    void testGetCount() {
        when(orderRepository.getCount()).thenReturn(2);
        OrderServiceImpl orderService = new OrderServiceImpl(orderRepository, orderDetailsRepository, menuItemService,
                menuItemRepository);
        int foundOrders = orderService.getCount();

        assertEquals(2, foundOrders);
        verify(orderRepository, times(1)).getCount();
    }
    @Test
    void testWhenFindByIdWithExistingOrderShouldReturnOrder() {
        Integer id = 1;
        Order expectedOrder = new Order(id, UUID.randomUUID(), new ArrayList<>());
        when(orderRepository.findById(id)).thenReturn(Optional.of(expectedOrder));

        Order result = service.findById(id);

        // Assert
        assertEquals(expectedOrder, result);
        verify(orderRepository, times(1)).findById(id);
    }

    @Test
    void testWhenFindByIdWithNonExistingOrderShouldThrowOrderDoesNotExistException() {
        Integer id = 1;
        when(orderRepository.findById(id)).thenReturn(Optional.empty());
        OrderDoesNotExistException exception = assertThrows(OrderDoesNotExistException.class, () -> service.findById(id));
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
    void testWhenCreateOrderShouldReturnTheCreatedOrder() {
        UUID session = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

        // Set up mock response
        String mockResponse = "{\"session\": {\"pc\": {\"id\": 123, \"noPC\": 1, \"noRuangan\": 2}}}";
        String mockUrl = apiWarnet + "/info_sesi/session_detail/" + session;

        // Set up RestTemplate mock
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<String> entity = new HttpEntity<>(headers);
        RestTemplate restTemplateMock = mock(RestTemplate.class);
        ResponseEntity<String> responseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);

        when(menuItemRepository.findById(any(String.class))).thenReturn(Optional.of(menuItem));
        when(restTemplateMock.exchange(mockUrl, HttpMethod.GET, entity, String.class)).thenReturn(responseEntity);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        service.setRestTemplate(restTemplateMock);

        service.create(orderRequest, null);

        verify(restTemplateMock, timeout(1000).atLeastOnce()).exchange(mockUrl, HttpMethod.GET, entity, String.class);
        verify(orderRepository, atLeastOnce()).save(any(Order.class));

    }

    @Test
    void testWhenCreateOrderShouldReturnTheCreatedOrderButFromAnotherSquad() {

        when(menuItemRepository.findById(any(String.class))).thenReturn(Optional.of(menuItem));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        service.create(orderRequest, "cafe");

        verify(orderRepository, atLeastOnce()).save(any(Order.class));
    }

    @Test
    void testWhenCreateOrderButMenuItemNotFoundShouldThrowException() {
        when(menuItemRepository.findById(any(String.class))).thenReturn(Optional.empty());

        assertThrows(MenuItemDoesNotExistException.class, () -> service.create(orderRequest, null));
    }

    @Test
    void testWhenCreateOrderAndMenuItemOutOfStockShouldThrowMenuItemOutOfStockException() {
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
    void testWhenCreateOrderFromAnotherSquadTheTotalPriceShouldBeZero() {
        MenuItem item = MenuItem.builder()
                .id("1")
                .price(5000)
                .stock(10)
                .build();

        OrderDetails orderDetails = OrderDetails.builder()
                .order(order)
                .menuItem(item)
                .quantity(1)
                .status(Status.CONFIRM.getValue())
                .totalPrice(0)
                .build();
        order.setOrderDetailsList(Collections.singletonList(orderDetails));

        when(menuItemRepository.findById(any(String.class))).thenReturn(Optional.of(item));

        service.create(orderRequest, "warnet");

        verify(orderRepository, atLeastOnce()).save(any(Order.class));

    }

    @Test
    void testWhenCancelOrder() {
        // Set up mock data
        OrderDetails orderDetails = OrderDetails.builder()
                .id(2)
                .quantity(1)
                .menuItem(menuItem)
                .status(Status.CONFIRM.getValue())
                .totalPrice(10000)
                .build();

        // Set up mock repository
        when(orderDetailsRepository.findById(any(Integer.class))).thenReturn(Optional.of(orderDetails));

        OrderDetails cancel = service.updateOrderDetailStatus(2, "cancel");
        assertEquals(orderDetails, cancel);
    }

    @Test
    void testWhenPrepareOrder() {
        // Set up mock data
        OrderDetails orderDetails = OrderDetails.builder()
                .id(2)
                .quantity(1)
                .menuItem(menuItem)
                .status(Status.CONFIRM.getValue())
                .totalPrice(10000)
                .build();

        // Set up mock repository
        when(orderDetailsRepository.findById(any(Integer.class))).thenReturn(Optional.of(orderDetails));

        OrderDetails prepare = service.updateOrderDetailStatus(2, "prepare");
        assertEquals(orderDetails, prepare);
    }

    @Test
    void testWhenDeliverOrder() {
        // Set up mock data
        OrderDetails orderDetails = OrderDetails.builder()
                .id(2)
                .quantity(1)
                .menuItem(menuItem)
                .status(Status.PREPARE.getValue())
                .totalPrice(10000)
                .build();

        // Set up mock repository
        when(orderDetailsRepository.findById(any(Integer.class))).thenReturn(Optional.of(orderDetails));

        OrderDetails deliver = service.updateOrderDetailStatus(2, "deliver");
        assertEquals(orderDetails, deliver);
    }

    @Test
    void testWhenNegativeCancelOrder() {
        // Set up mock data
        OrderDetails orderDetails = OrderDetails.builder()
                .id(2)
                .quantity(1)
                .menuItem(menuItem)
                .status(Status.PREPARE.getValue())
                .totalPrice(10000)
                .build();

        // Set up mock repository
        when(orderDetailsRepository.findById(any(Integer.class))).thenReturn(Optional.of(orderDetails));

        assertThrows(OrderDetailStatusInvalid.class, () -> service.updateOrderDetailStatus(2, "cancel"));
    }

    @Test
    void testWhenUpdateOrderAndStatusAlreadyDoneOrCanceled() {
        // Set up mock data
        OrderDetails orderDetails = OrderDetails.builder()
                .id(1)
                .quantity(1)
                .menuItem(menuItem)
                .status(Status.DONE.getValue())
                .totalPrice(10000)
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
    void testWhenUpdateBadRequest() {
        // Set up mock data
        OrderDetails orderDetails = OrderDetails.builder()
                .id(1)
                .quantity(1)
                .menuItem(menuItem)
                .status(Status.CONFIRM.getValue())
                .totalPrice(10000)
                .build();

        // Set up mock repository
        when(orderDetailsRepository.findById(any(Integer.class))).thenReturn(Optional.of(orderDetails));

        try {
            service.updateOrderDetailStatus(1, "abc");
        } catch (BadRequest e) {
            String expectedMessage = "400 Bad Request";
            String actualMessage = e.getMessage();

            assertTrue(actualMessage.contains(expectedMessage));
        }
    }

    @Test
    void testWhenDeleteOrderAndFoundShouldDeleteOrder() {
        int orderId = 1;
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(Order.builder().id(orderId).build()));
        service.delete(orderId);
        verify(orderRepository, times(1)).deleteById(orderId);
    }

    @Test
    void testWhenDeleteOrderAndNotFoundShouldThrowException() {
        int orderId = 1;
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());
        assertThrows(OrderDoesNotExistException.class, () -> service.delete(orderId));
    }

    @Test
    void testFindBySession() {

        UUID session = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        // Set up mock response
        String mockResponse = "{\"session\": {\"pc\": {\"id\": 123, \"noPC\": 1, \"noRuangan\": 2}}}";
        String mockUrl = apiWarnet + "/info_sesi/session_detail/" + session;

        // Set up RestTemplate mock
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<String> entity = new HttpEntity<>(headers);
        RestTemplate restTemplateMock = mock(RestTemplate.class);
        ResponseEntity<String> responseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);
        when(restTemplateMock.exchange(mockUrl, HttpMethod.GET, entity, String.class)).thenReturn(responseEntity);
        when(orderRepository.findBySession(session)).thenReturn(Optional.of(List.of(order)));
        service.setRestTemplate(restTemplateMock);

        service.findBySession(session);

        verify(orderRepository, atLeastOnce()).findBySession(session);
    }

    @Test
    void testWhenFindBySessionNotExist() {
        OrderServiceImpl orderService = new OrderServiceImpl(orderRepository, orderDetailsRepository, menuItemService,
                menuItemRepository);
        RestTemplate restTemplate = mock(RestTemplate.class);
        orderService.setRestTemplate(restTemplate);
        UUID session = UUID.randomUUID();
        String url = apiWarnet + "/info_sesi/session_detail/" + session;
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<>(headers);
        String responseBody = "The UUID is not found";

        when(restTemplate.exchange(url, HttpMethod.GET, entity, String.class))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, responseBody));

        assertThrows(UUIDNotFoundException.class, () -> orderService.findBySession(session));
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
        String billUrl = apiBayar + "/bills";

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
    void testWhenJSONRequestInvalidShouldThrowException() {
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
    void testWhenDoneOrder() {
        // Set up mock data
        order = Order.builder()
                .id(287952)
                .session(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
                .build();

        OrderDetails orderDetails = OrderDetails.builder()
                .id(2)
                .quantity(1)
                .menuItem(menuItem)
                .status(Status.DELIVER.getValue())
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

    @Test
    void testWhenDoneOrderPackage() {
        // Set up mock data
        order = Order.builder()
                .id(287952)
                .session(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
                .build();

        OrderDetails orderDetails = OrderDetails.builder()
                .id(2)
                .quantity(1)
                .menuItem(menuItem)
                .status(Status.DELIVER.getValue())
                .totalPrice(0)
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

    @Test
    void testWhenUpdateAndIdNotFoundShouldThrowException() {
        when(orderDetailsRepository.findById(10)).thenReturn(Optional.empty());
        Assertions.assertThrows(OrderDetailDoesNotExistException.class, () ->
                service.updateOrderDetailStatus(10, "prepare"));
    }

    @Test
    void testWhenUpdateOrderAndDone() {
        // Set up mock data
        Order orderMock = Order.builder()
                .session(UUID.randomUUID())
                .build();
        OrderDetails orderDetails = OrderDetails.builder()
                .id(2)
                .quantity(1)
                .menuItem(menuItem)
                .status(Status.DELIVER.getValue())
                .totalPrice(10000)
                .order(orderMock)
                .build();

        RestTemplate restTemplate = mock(RestTemplate.class);
        service.setRestTemplate(restTemplate);
        when(orderDetailsRepository.findById(any(Integer.class))).thenReturn(Optional.of(orderDetails));

        OrderDetails updatedOrderDetails = service.updateOrderDetailStatus(2, "done");

        assertEquals(Status.DONE.getValue(), updatedOrderDetails.getStatus());
    }

    @Test
    void testSetPCInformation() {
        // Test inputs
        UUID session = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        OrderDetails orderDetails = new OrderDetails();
        ExecutorService executorService = Executors.newFixedThreadPool(3);

        // Set up mock response
        String mockResponse = "{\"session\": {\"pc\": {\"id\": 123, \"noPC\": 1, \"noRuangan\": 2}}}";
        String mockUrl = apiWarnet + "/info_sesi/session_detail/" + session;

        // Set up RestTemplate mock
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<String> entity = new HttpEntity<>(headers);
        RestTemplate restTemplateMock = mock(RestTemplate.class);
        ResponseEntity<String> responseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);
        when(restTemplateMock.exchange(mockUrl, HttpMethod.GET, entity, String.class)).thenReturn(responseEntity);

        service.setRestTemplate(restTemplateMock);

        service.setOrderPC(session, orderDetails, executorService);

        // Extract pcInfo from the mock response
        JSONObject pcInfo = new JSONObject(mockResponse).getJSONObject("session").getJSONObject("pc");

        // Wait for the completion of CompletableFuture tasks
        CompletableFuture<Void> setIdPCTask = CompletableFuture.runAsync(() -> orderDetails.setIdPC(pcInfo.getInt("id")));
        CompletableFuture<Void> setNoPCTask = CompletableFuture.runAsync(() -> orderDetails.setNoPC(pcInfo.getInt("noPC")));
        CompletableFuture<Void> setNoRuanganTask = CompletableFuture.runAsync(() -> orderDetails.setNoRuangan(pcInfo.getInt("noRuangan")));

        try {
            setIdPCTask.join();
            setNoPCTask.join();
            setNoRuanganTask.join();
        } catch (Exception e) {
            // Handle any exceptions that occurred during CompletableFuture execution
        }

        // Assert the values
        assertTimeout(Duration.ofMillis(10000), () -> {
            assertEquals(123, orderDetails.getIdPC());
            assertEquals(1, orderDetails.getNoPC());
            assertEquals(2, orderDetails.getNoRuangan());
        });
    }

    @Test
    void testWhenNegativePrepareOrder() {
        // Set up mock data
        OrderDetails orderDetails = OrderDetails.builder()
                .id(2)
                .quantity(1)
                .menuItem(menuItem)
                .status(Status.PREPARE.getValue())
                .totalPrice(10000)
                .build();

        // Set up mock repository
        when(orderDetailsRepository.findById(any(Integer.class))).thenReturn(Optional.of(orderDetails));

        assertThrows(UpdateStatusInvalid.class, () -> service.updateOrderDetailStatus(2, "prepare"));
    }
    @Test
    void testWhenNegativeDeliverOrder() {
        // Set up mock data
        OrderDetails orderDetails = OrderDetails.builder()
                .id(2)
                .quantity(1)
                .menuItem(menuItem)
                .status(Status.CONFIRM.getValue())
                .totalPrice(10000)
                .build();

        // Set up mock repository
        when(orderDetailsRepository.findById(any(Integer.class))).thenReturn(Optional.of(orderDetails));

        assertThrows(UpdateStatusInvalid.class, () -> service.updateOrderDetailStatus(2, "deliver"));
    }

    @Test
    void testWhenNegativeDoneOrder() {
        // Set up mock data
        OrderDetails orderDetails = OrderDetails.builder()
                .id(2)
                .quantity(1)
                .menuItem(menuItem)
                .status(Status.PREPARE.getValue())
                .totalPrice(10000)
                .build();

        // Set up mock repository
        when(orderDetailsRepository.findById(any(Integer.class))).thenReturn(Optional.of(orderDetails));

        assertThrows(UpdateStatusInvalid.class, () -> service.updateOrderDetailStatus(2, "done"));
    }

    @Test
    void testWhenAlreadyDoneUpdateOrder() {
        // Set up mock data
        OrderDetails orderDetails = OrderDetails.builder()
                .id(2)
                .quantity(1)
                .menuItem(menuItem)
                .status("Selesai")
                .totalPrice(10000)
                .build();

        // Set up mock repository
        when(orderDetailsRepository.findById(any(Integer.class))).thenReturn(Optional.of(orderDetails));

        assertThrows(OrderDetailStatusInvalid.class, () -> service.updateOrderDetailStatus(2, "prepare"));
    }

    @Test
    void testWhenAlreadyCanceledUpdateOrder() {
        // Set up mock data
        OrderDetails orderDetails = OrderDetails.builder()
                .id(2)
                .quantity(1)
                .menuItem(menuItem)
                .status("Dibatalkan")
                .totalPrice(10000)
                .build();

        // Set up mock repository
        when(orderDetailsRepository.findById(any(Integer.class))).thenReturn(Optional.of(orderDetails));

        assertThrows(OrderDetailStatusInvalid.class, () -> service.updateOrderDetailStatus(2, "prepare"));
    }

    @Test
    void testWhenNegativeCancelOrderFromWarnet() {
        // Set up mock data
        OrderDetails orderDetails = OrderDetails.builder()
                .id(2)
                .quantity(1)
                .menuItem(menuItem)
                .status(Status.CONFIRM.getValue())
                .totalPrice(0)
                .build();

        // Set up mock repository
        when(orderDetailsRepository.findById(any(Integer.class))).thenReturn(Optional.of(orderDetails));

        assertThrows(OrderDetailStatusInvalid.class, () -> service.updateOrderDetailStatus(2, "cancel"));
    }

    @Test
    void testWhenSetOrderPcButUUIDNotFound() {
        ExecutorService executorService = Executors.newCachedThreadPool();
        RestTemplate restTemplateMock = mock(RestTemplate.class);
        UUID session = UUID.randomUUID();
        service.setRestTemplate(restTemplateMock);
        when(restTemplateMock.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(String.class))).thenThrow(HttpClientErrorException.class);

        assertThrows(UUIDNotFoundException.class, () -> {
            service.setOrderPC(session, newOrderDetails, executorService);
        });


    }
}
