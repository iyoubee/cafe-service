package id.ac.ui.cs.advprog.cafeservice.controller;

import id.ac.ui.cs.advprog.cafeservice.Util;
import id.ac.ui.cs.advprog.cafeservice.dto.OrderDetailsData;
import id.ac.ui.cs.advprog.cafeservice.dto.OrderRequest;
import id.ac.ui.cs.advprog.cafeservice.exceptions.OrderDoesNotExistException;
import id.ac.ui.cs.advprog.cafeservice.model.menu.MenuItem;
import id.ac.ui.cs.advprog.cafeservice.model.order.Order;
import id.ac.ui.cs.advprog.cafeservice.model.order.OrderDetails;
import id.ac.ui.cs.advprog.cafeservice.service.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = OrderController.class)
@AutoConfigureMockMvc
class OrderControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private OrderServiceImpl service;

    Order newOrder;

    OrderDetails orderDetails;

    Order badRequest;

    MenuItem menuItem;

    Object bodyContent;

    @BeforeEach
    void setUp() {

        menuItem = MenuItem.builder()
            .name("Indomie")
            .price(10000)
            .stock(4)
            .build();
        
        newOrder = Order.builder()
            .session(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
            .orderDetailsList(Arrays.asList(
                OrderDetails.builder()
                    .order(newOrder)
                    .menuItem(menuItem)
                    .quantity(1)
                    .status("Menunggu konfirmasi")
                    .totalPrice(10000)
                    .build()
            ))
        .build();

        orderDetails = OrderDetails.builder()
            .id(100)
            .order(newOrder)
            .status("Menunggu Konfirmasi")
            .menuItem(menuItem)
            .quantity(1)
            .totalPrice(10000)
            .build();

        badRequest = Order.builder()
            .session(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
            .orderDetailsList(null)
            .build();

        OrderDetailsData orderDetailsDataRequest = new OrderDetailsData();
        orderDetailsDataRequest.setQuantity(1);
        orderDetailsDataRequest.setMenuItemId(String.valueOf(UUID.randomUUID()));

        bodyContent = new Object() {
            public final UUID session = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            public final List<OrderDetailsData> orderDetailsData = Arrays.asList(orderDetailsDataRequest);
        };

    }

    @Test
    void testGetOrderBySession() throws Exception {
        List<Order> listOrder = new ArrayList<>();
        listOrder.add(newOrder);
        when(service.findBySession(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))).thenReturn(listOrder);

        mvc.perform(get("/cafe/order/123e4567-e89b-12d3-a456-426614174000")
            .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(handler().methodName("getOrderBySession"))
                .andExpect(jsonPath("$[0].session").value(newOrder.getSession().toString()));

        verify(service, atLeastOnce()).findBySession(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
    }

    @Test
    void testGetOrderBySessionAndSessionNotUUID() throws Exception {
        List<Order> listOrder = new ArrayList<>();
        listOrder.add(newOrder);

        mvc.perform(get("/cafe/order/abc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(handler().methodName("getOrderBySession"))
                .andExpect(jsonPath("$.message").value("Invalid UUID string"));
    }

    @Test
    void testGetAllOrder() throws Exception {
        mvc.perform(get("/cafe/order/all")
            .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(handler().methodName("getAllOrder"));

        verify(service, atLeastOnce()).findAll();
    }
    @Test
    void testGetOrderByPage() throws Exception {
        mvc.perform(get("/cafe/order/all/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(handler().methodName("getOrderByPagination"));

        verify(service, atLeastOnce()).findByPagination(1);
    }

    @Test
    void testCountOrder() throws Exception {
        mvc.perform(get("/cafe/order/all/count")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(handler().methodName("getCount"));

        verify(service, atLeastOnce()).getCount();
    }

    @Test
    void testGetOrderById() throws Exception {
        mvc.perform(get("/cafe/order/id/1")
            .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(handler().methodName("getOrderById"));

        verify(service, atLeastOnce()).findById(any(Integer.class));
    }

    @Test
    void testCreateOrder() throws Exception {
        when(service.create(any(OrderRequest.class), eq(null))).thenReturn(newOrder);

        mvc.perform(post("/cafe/order/create")
            .contentType(MediaType.APPLICATION_JSON)
            .content(Util.mapToJson(bodyContent)))
                .andExpect(status().isOk())
                .andExpect(handler().methodName("createOrder"));

        verify(service, atLeastOnce()).create(any(OrderRequest.class), eq(null));
    }

    @Test
    void testChangeStatus() throws Exception {
        when(service.updateOrderDetailStatus(any(Integer.class), any(String.class))).thenReturn(orderDetails);

        mvc.perform(put("/cafe/order/update/1?status=prepare")
            .contentType(MediaType.APPLICATION_JSON)
            .content(Util.mapToJson(bodyContent)))
                .andExpect(status().isOk())
                .andExpect(handler().methodName("changeStatus"))
                .andExpect(jsonPath("$.status").value("Menunggu Konfirmasi"));

        verify(service, atLeastOnce()).updateOrderDetailStatus(any(Integer.class), any(String.class));
    }


    @Test
    void testDeleteOrder() throws Exception {
        mvc.perform(delete("/cafe/order/delete/1")
            .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(handler().methodName("deleteOrder"));

        verify(service, atLeastOnce()).delete(any(Integer.class));
    }

    @Test
    void testGetOrderByIdShouldThrowOrderDoesNotExistException() throws Exception {
        when(service.findById(anyInt())).thenThrow(OrderDoesNotExistException.class);

        mvc.perform(get("/cafe/order/id/1")
            .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetOrderBySessionShouldThrowOrderDoesNotExistException() throws Exception {
        when(service.findBySession(any(UUID.class))).thenThrow(OrderDoesNotExistException.class);

        mvc.perform(get("/cafe/order/" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateOrderWithNullValueShouldThrowException() throws Exception {
        bodyContent = new Object() {
            public final UUID session = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            public final List<OrderDetailsData> orderDetailsData = null;
        };

        mvc.perform(post("/cafe/order/create")
            .contentType(MediaType.APPLICATION_JSON)
            .content(Util.mapToJson(bodyContent)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateOrderWithMenuItemIdIsNull() throws Exception {
        OrderDetailsData dataRequest = OrderDetailsData.builder()
                .menuItemId(null)
                .quantity(10)
                .build();

        bodyContent = new Object() {
            public final UUID session = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            public final List<OrderDetailsData> orderDetailsData = List.of(dataRequest);
        };

        mvc.perform(post("/cafe/order/create")
            .contentType(MediaType.APPLICATION_JSON)
            .content(Util.mapToJson(bodyContent)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateOrderWithMenuItemIdIsEmpty() throws Exception {
        OrderDetailsData dataRequest = OrderDetailsData.builder()
                .menuItemId("")
                .quantity(10)
                .build();

        bodyContent = new Object() {
            public final UUID session = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            public final List<OrderDetailsData> orderDetailsData = List.of(dataRequest);
        };

        mvc.perform(post("/cafe/order/create")
            .contentType(MediaType.APPLICATION_JSON)
            .content(Util.mapToJson(bodyContent)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateOrderWithInvalidQuantity() throws Exception {
        OrderDetailsData dataRequest = OrderDetailsData.builder()
                .menuItemId(String.valueOf(UUID.randomUUID()))
                .quantity(-10)
                .build();

        bodyContent = new Object() {
            public final UUID session = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            public final List<OrderDetailsData> orderDetailsData = List.of(dataRequest);
        };

        mvc.perform(post("/cafe/order/create")
            .contentType(MediaType.APPLICATION_JSON)
            .content(Util.mapToJson(bodyContent)))
                .andExpect(status().isBadRequest());
    }
}