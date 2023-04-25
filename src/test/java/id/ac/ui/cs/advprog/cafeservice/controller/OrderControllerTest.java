package id.ac.ui.cs.advprog.cafeservice.controller;

import id.ac.ui.cs.advprog.cafeservice.Util;
import id.ac.ui.cs.advprog.cafeservice.dto.OrderDetailsData;
import id.ac.ui.cs.advprog.cafeservice.dto.OrderRequest;
import id.ac.ui.cs.advprog.cafeservice.exceptions.BadRequest;
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

import static org.junit.jupiter.api.Assertions.*;
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
                .status("Approved")
                .build()
        ))
        .build();

        badRequest = Order.builder()
        .session(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
        .orderDetailsList(null)
        .build();

        OrderDetailsData orderDetailsData = new OrderDetailsData();
        orderDetailsData.setQuantity(1);
        orderDetailsData.setStatus("Approved");

        bodyContent = new Object() {
            public final UUID session = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            public final List<OrderDetailsData> orderDetailsList = Arrays.asList(orderDetailsData);
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
    void testGetAllOrder() throws Exception {
        mvc.perform(get("/cafe/order/all")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(handler().methodName("getAllOrder"));

        verify(service, atLeastOnce()).findAll();
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
        when(service.create(any(OrderRequest.class))).thenReturn(newOrder);

        mvc.perform(post("/cafe/order/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(Util.mapToJson(bodyContent)))
                .andExpect(status().isOk())
                .andExpect(handler().methodName("createOrder"));

        verify(service, atLeastOnce()).create(any(OrderRequest.class));
    }

    @Test
    void testChangeStatus() throws Exception {
        when(service.update(any(Integer.class), any(OrderRequest.class))).thenReturn(newOrder);

        mvc.perform(put("/cafe/order/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(Util.mapToJson(bodyContent)))
                .andExpect(status().isOk())
                .andExpect(handler().methodName("changeStatus"))
                .andExpect(jsonPath("$.session").value("123e4567-e89b-12d3-a456-426614174000"));

        verify(service, atLeastOnce()).update(any(Integer.class), any(OrderRequest.class));
    }

    @Test
    void testChangeStatusWhenOrderRequestValueIsNull() throws Exception {
        when(service.update(any(Integer.class), any(OrderRequest.class))).thenReturn(badRequest);

        try {
            mvc.perform(put("/cafe/order/update/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(Util.mapToJson(bodyContent)));
        }catch (BadRequest e) {
            String expectedMessage = "400 Bad Request";
            String actualMessage = e.getMessage();

            assertTrue(actualMessage.contains(expectedMessage));
        }
    }

    @Test
    void testDeleteOrder() throws Exception {
        mvc.perform(delete("/cafe/order/delete/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(handler().methodName("deleteOrder"));

        verify(service, atLeastOnce()).delete(any(Integer.class));
    }
}