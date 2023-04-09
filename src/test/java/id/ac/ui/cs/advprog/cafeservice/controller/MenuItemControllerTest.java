package id.ac.ui.cs.advprog.cafeservice.controller;

import id.ac.ui.cs.advprog.cafeservice.Util;
import id.ac.ui.cs.advprog.cafeservice.dto.MenuItemRequest;
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

    Object bodyContent;

    @BeforeEach
    void setUp() {
        menuItem = MenuItem.builder()
                .name("Indomie")
                .price(5000)
                .stock(100)
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
        when(service.findById(any(Integer.class))).thenReturn(menuItem);

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
    void testPutMenuItem() throws Exception {
        when(service.update(any(Integer.class), any(MenuItemRequest.class))).thenReturn(menuItem);

        mvc.perform(put("/cafe/menu/update/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(Util.mapToJson(bodyContent)))
                .andExpect(status().isOk())
                .andExpect(handler().methodName("putMenuItem"))
                .andExpect(jsonPath("$.name").value(menuItem.getName()));

        verify(service, atLeastOnce()).update(any(Integer.class), any(MenuItemRequest.class));
    }

    @Test
    void testDeleteMenuItem() throws Exception {
        mvc.perform((delete("/cafe/menu/delete/1")
                .contentType(MediaType.APPLICATION_JSON)))
                .andExpect(status().isOk())
                .andExpect(handler().methodName("deleteMenuItem"));

        verify(service, atLeastOnce()).delete(any(Integer.class));
    }

}
