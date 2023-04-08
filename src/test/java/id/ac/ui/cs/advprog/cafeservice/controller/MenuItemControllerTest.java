package id.ac.ui.cs.advprog.cafeservice.controller;

import id.ac.ui.cs.advprog.cafeservice.Util;
import id.ac.ui.cs.advprog.cafeservice.dto.MenuItemRequest;
import id.ac.ui.cs.advprog.cafeservice.exceptions.BadRequest;
import id.ac.ui.cs.advprog.cafeservice.model.menu.MenuItem;
import id.ac.ui.cs.advprog.cafeservice.service.MenuItemServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = MenuItemController.class)
@AutoConfigureMockMvc
class MenuItemControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private MenuItemServiceImpl service;

    MenuItem menuItem;

    MenuItem badRequest;

    MenuItem emptyName;

    MenuItem invalidValue;

    Object bodyContent;

    @BeforeEach
    void setUp() {
        menuItem = MenuItem.builder()
                .name("Indomie")
                .price(5000)
                .stock(100)
                .build();

        badRequest = MenuItem.builder()
            .name("Indomie")
            .build();

        invalidValue = MenuItem.builder()
        .name("Indomie")
        .price(-100)
        .stock(-1)
        .build();

        emptyName =  MenuItem.builder()
        .name("")
        .price(1000)
        .stock(2)
        .build();

        bodyContent = new Object() {
            public final String name = "Indomie";

            public final int price = 5000;

            public final int stock = 100;
        };
    }

    @Test
    void testGetAllMenuItem() throws Exception {
        List<MenuItem> allMenuItem = List.of(menuItem);

        when(service.findAll()).thenReturn(allMenuItem);

        mvc.perform(get("/cafe/menu/all")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(handler().methodName("getAllMenuItem"))
                .andExpect(jsonPath("$[0].name").value(menuItem.getName()));

        verify(service, atLeastOnce()).findAll();
    }

    @Test
    void testGetMenuItemById() throws Exception {
        when(service.findById(any(String.class))).thenReturn(menuItem);

        mvc.perform(get("/cafe/menu/id/1")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(handler().methodName("getMenuItemById"))
                .andExpect(jsonPath("$.name").value(menuItem.getName()));
    }

    @Test
    void testAddMenuItem() throws Exception {
        when(service.create(any(MenuItemRequest.class))).thenReturn(menuItem);

        mvc.perform(post("/cafe/menu/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(Util.mapToJson(bodyContent)))
                .andExpect(status().isOk())
                .andExpect(handler().methodName("addMenuItem"))
                .andExpect(jsonPath("$.name").value(menuItem.getName()));

        verify(service, atLeastOnce()).create(any(MenuItemRequest.class));
    }

    @Test
    void testBadRequestAddMenuItem() throws Exception {
        when(service.create(any(MenuItemRequest.class))).thenReturn(badRequest);

        Exception exception = assertThrows(BadRequest.class, () -> {
            mvc.perform(post("/cafe/menu/create")
            .contentType(MediaType.APPLICATION_JSON)
            .content(Util.mapToJson(bodyContent)));
        });

        String expectedMessage = "400 Bad Request";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void testInvalidValueAddMenuItem() throws Exception {
        when(service.create(any(MenuItemRequest.class))).thenReturn(invalidValue);

        Exception exception = assertThrows(BadRequest.class, () -> {
            mvc.perform(post("/cafe/menu/create")
            .contentType(MediaType.APPLICATION_JSON)
            .content(Util.mapToJson(bodyContent)));
        });

        String expectedMessage = "The value of Price is invalid";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void testNameEmptyAddMenuItem() throws Exception {
        when(service.create(any(MenuItemRequest.class))).thenReturn(emptyName);

        Exception exception = assertThrows(BadRequest.class, () -> {
            mvc.perform(post("/cafe/menu/create")
            .contentType(MediaType.APPLICATION_JSON)
            .content(Util.mapToJson(bodyContent)));
        });

        String expectedMessage = "The menu item Name request can't be empty";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void testPutMenuItem() throws Exception {
        when(service.update(any(String.class), any(MenuItemRequest.class))).thenReturn(menuItem);

        mvc.perform(put("/cafe/menu/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(Util.mapToJson(bodyContent)))
                .andExpect(status().isOk())
                .andExpect(handler().methodName("putMenuItem"))
                .andExpect(jsonPath("$.name").value(menuItem.getName()));

        verify(service, atLeastOnce()).update(any(String.class), any(MenuItemRequest.class));
    }

}