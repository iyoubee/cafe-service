package id.ac.ui.cs.advprog.cafeservice.controller;

import id.ac.ui.cs.advprog.cafeservice.Util;
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

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
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
        .pc(1204)
        .orderDetailsList(Arrays.asList(
            OrderDetails.builder()
                .menuItem(menuItem)
                .quantity(1)
                .status("Approved")
                .totalPrice(10000)
                .build()
        ))
        .build();

        badRequest = Order.builder()
        .pc(1204)
        .orderDetailsList(null)
        .build();

        bodyContent = new Object() {
            public final Integer pc = 1204;

            public final List<OrderDetails> orderDetailsList = Arrays.asList(
                OrderDetails.builder()
                    .menuItem(menuItem)
                    .quantity(1)
                    .status("Approved")
                    .totalPrice(10000)
                    .build()
            );
        };
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
                .andExpect(jsonPath("$.pc").value(1204));

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
}