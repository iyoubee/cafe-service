package id.ac.ui.cs.advprog.cafeservice.service.menu;

import id.ac.ui.cs.advprog.cafeservice.dto.OrderDetailsData;
import id.ac.ui.cs.advprog.cafeservice.dto.OrderRequest;
import id.ac.ui.cs.advprog.cafeservice.exceptions.*;
import id.ac.ui.cs.advprog.cafeservice.model.menu.MenuItem;
import id.ac.ui.cs.advprog.cafeservice.model.order.Order;
import id.ac.ui.cs.advprog.cafeservice.model.order.OrderDetails;
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
import org.springframework.web.client.RestTemplate;

import java.util.*;
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

    Order order;

    Order newOrder;

    Order createdOrder;

    OrderDetails newOrderDetails;

    OrderDetailsData newOrderDetailsData;

    MenuItem menuItem;

    OrderRequest orderRequest;

    @Value("${ENV_API_WARNET}")
    private String API_WARNET;
    @Value("${ENV_API_BAYAR}")
    private String API_BAYAR;

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
        List<Order> orders = List.of(
                Order.builder().id(1).session(UUID.fromString("123e4567-e89b-12d3-a456-426614174000")).build(),
                Order.builder().id(2).session(UUID.fromString("123e4567-e89b-12d3-a456-426614174001")).build());
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
        Integer totalPrice = 10000;
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
    void testWhenCreateOrderShouldReturnTheCreatedOrder() {
        UUID session = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        // Set up mock response
        String mockResponse = "{\"session\": {\"pc\": {\"id\": 123, \"noPC\": 1, \"noRuangan\": 2}}}";
        String mockUrl = API_WARNET + "/info_sesi/session_detail/" + session;

        // Set up RestTemplate mock
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<String> entity = new HttpEntity<>(headers);
        RestTemplate restTemplateMock = mock(RestTemplate.class);
        ResponseEntity<String> responseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);
        when(restTemplateMock.exchange(mockUrl, HttpMethod.GET, entity, String.class)).thenReturn(responseEntity);
        when(menuItemRepository.findById(any(String.class))).thenReturn(Optional.of(menuItem));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        service.setRestTemplate(restTemplateMock);

        Order result = service.create(orderRequest, null);

        verify(orderRepository, atLeastOnce()).save(any(Order.class));
    }

    @Test
    void testWhenCreateOrderButMenuItemNotFoundShouldThrowException() {
        when(menuItemRepository.findById(any(String.class))).thenReturn(Optional.empty());

        assertThrows(MenuItemDoesNotExistException.class, () -> {
            service.create(orderRequest, null);
        });
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

        UUID session = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        String pcUrl = API_WARNET + "/info_sesi/session_detail/" + session;
        JSONObject pcResponse = new JSONObject();
        pcResponse.put("id", 1);
        pcResponse.put("noPC", 1);
        pcResponse.put("noRuangan", 1);

        JSONObject sessionResponse = new JSONObject();
        sessionResponse.put("pc", pcResponse);

        JSONObject response = new JSONObject();
        response.put("session", sessionResponse);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<String> entity = new HttpEntity<>(headers);
        RestTemplate restTemplateMock = mock(RestTemplate.class);
        ResponseEntity<String> responseEntity = new ResponseEntity<>(response.toString(), HttpStatus.OK);
        when(restTemplateMock.exchange(pcUrl, HttpMethod.GET, entity, String.class)).thenReturn(responseEntity);

        service.setRestTemplate(restTemplateMock);

        when(menuItemRepository.findById(any(String.class))).thenReturn(Optional.of(item));

        Order result = service.create(orderRequest, "warnet");

        verify(orderRepository, atLeastOnce()).save(any(Order.class));

    }

    @Test
    void testWhenCancelOrder() {
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
    void testWhenPrepareOrder() {
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
    void testWhenDeliverOrder() {
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
                .status("Sedang disiapkan")
                .totalPrice(10000)
                .build();

        // Set up mock repository
        when(orderDetailsRepository.findById(any(Integer.class))).thenReturn(Optional.of(orderDetails));

        assertThrows(OrderDetailStatusInvalid.class, () -> {
            service.updateOrderDetailStatus(2, "cancel");
        });
    }

    @Test
    void testWhenUpdateOrderAndStatusAlreadyDoneOrCanceled() {
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
    void testWhenUpdateBadRequest() {
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
    void testWhenFindBySessionNotExist() {
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
        String billUrl = API_BAYAR + "/bills";

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
                .status("Sedang Diantar")
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
                .status("Sedang Diantar")
                .totalPrice(10000)
                .order(orderMock)
                .build();

        RestTemplate restTemplate = mock(RestTemplate.class);
        service.setRestTemplate(restTemplate);
        when(orderDetailsRepository.findById(any(Integer.class))).thenReturn(Optional.of(orderDetails));

        OrderDetails updatedOrderDetails = service.updateOrderDetailStatus(2, "done");

        assertEquals("Selesai", updatedOrderDetails.getStatus());
    }

    @Test
    void testSetPCInformation() throws Exception {
        UUID session = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        OrderDetails orderDetails = new OrderDetails();
        ExecutorService executorService = Executors.newFixedThreadPool(3);

        // Set up mock response
        String mockResponse = "{\"session\": {\"pc\": {\"id\": 123, \"noPC\": 1, \"noRuangan\": 2}}}";
        String mockUrl = API_WARNET + "/info_sesi/session_detail/" + session;

        // Set up RestTemplate mock
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<String> entity = new HttpEntity<>(headers);
        RestTemplate restTemplateMock = mock(RestTemplate.class);
        ResponseEntity<String> responseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);
        when(restTemplateMock.exchange(mockUrl, HttpMethod.GET, entity, String.class)).thenReturn(responseEntity);

        service.setRestTemplate(restTemplateMock);

        service.setOrderPC(session, orderDetails, executorService);
        assertEquals(123, orderDetails.getIdPC());
        assertEquals(1, orderDetails.getNoPC());
        assertEquals(2, orderDetails.getNoRuangan());

    }

    @Test
    void testWhenNegativePrepareOrder() {
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

        assertThrows(UpdateStatusInvalid.class, () -> {
            service.updateOrderDetailStatus(2, "prepare");
        });
    }
    @Test
    void testWhenNegativeDeliverOrder() {
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

        assertThrows(UpdateStatusInvalid.class, () -> {
            service.updateOrderDetailStatus(2, "deliver");
        });
    }

    @Test
    void testWhenNegativeDoneOrder() {
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

        assertThrows(UpdateStatusInvalid.class, () -> {
            service.updateOrderDetailStatus(2, "done");
        });
    }
    @Test
    void testWhenNegativeCancelOrderFromWarnet() {
        // Set up mock data
        OrderDetails orderDetails = OrderDetails.builder()
                .id(2)
                .quantity(1)
                .menuItem(menuItem)
                .status("Menunggu Konfirmasi")
                .totalPrice(0)
                .build();

        // Set up mock repository
        when(orderDetailsRepository.findById(any(Integer.class))).thenReturn(Optional.of(orderDetails));

        assertThrows(OrderDetailStatusInvalid.class, () -> {
            service.updateOrderDetailStatus(2, "cancel");
        });
    }


}
