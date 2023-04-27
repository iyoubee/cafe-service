package id.ac.ui.cs.advprog.cafeservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.cafeservice.Util;
import id.ac.ui.cs.advprog.cafeservice.dto.MenuItemRequest;
import id.ac.ui.cs.advprog.cafeservice.exceptions.BadRequest;
import id.ac.ui.cs.advprog.cafeservice.exceptions.MenuItemValueEmpty;
import id.ac.ui.cs.advprog.cafeservice.exceptions.MenuItemValueInvalid;
import id.ac.ui.cs.advprog.cafeservice.model.menu.MenuItem;
import id.ac.ui.cs.advprog.cafeservice.service.MenuItemServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
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
            .price(null)
            .stock(4)
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

        when(service.findAll(null)).thenReturn(allMenuItem);

        mvc.perform(get("/cafe/menu/all")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(handler().methodName("getAllMenuItem"))
                .andExpect(jsonPath("$[0].name").value(menuItem.getName()));

        verify(service, atLeastOnce()).findAll(null);
    }

    @Test
    void testGetAvailableMenuItem() throws Exception {
        List<MenuItem> availableMenuItem = List.of(menuItem);

        when(service.findAll("available")).thenReturn(availableMenuItem);

        mvc.perform(get("/cafe/menu/all?query=available")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(handler().methodName("getAllMenuItem"))
                .andExpect(jsonPath("$[0].name").value(menuItem.getName()));

        verify(service, atLeastOnce()).findAll("available");
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
    void testInvalidValueAddMenuItem() throws Exception {
        when(service.create(any(MenuItemRequest.class))).thenReturn(invalidValue);

        try {
            mvc.perform(post("/cafe/menu/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(Util.mapToJson(bodyContent)));
        } catch (Exception e) {
            String expectedMessage = "The value of Price is invalid";
            String actualMessage = e.getMessage();

            assertTrue(actualMessage.contains(expectedMessage));
        }
    }

    @Test
    void testNameEmptyAddMenuItem() throws Exception {
        when(service.create(any(MenuItemRequest.class))).thenReturn(emptyName);

        try {
            mvc.perform(post("/cafe/menu/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(Util.mapToJson(bodyContent)));
        } catch (Exception e) {
            String expectedMessage = "The menu item Name request can't be empty";
            String actualMessage = e.getMessage();

            assertTrue(actualMessage.contains(expectedMessage));
        }
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

    @Test
    void testPutMenuItemWhenMenuItemValueIsNull() throws Exception {
        when(service.update(any(String.class), any(MenuItemRequest.class))).thenReturn(badRequest);

        try {
            mvc.perform(put("/cafe/menu/update/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(Util.mapToJson(bodyContent)));
        }catch (BadRequest e) {
            Assertions.assertEquals(BadRequest.class, e.getClass());
        }
    }

    @Test
    void testPutMenuItemWhenMenuItemValueIsEmpty() throws Exception {
        when(service.update(any(String.class), any(MenuItemRequest.class))).thenReturn(emptyName);

        try {
            mvc.perform(put("/cafe/menu/update/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(Util.mapToJson(bodyContent)));
        }catch (MenuItemValueEmpty e) {
            Assertions.assertEquals(MenuItemValueEmpty.class, e.getClass());
        }
    }

    @Test
    void testPutMenuItemWhenMenuItemValueIsInvalid() throws Exception {
        when(service.update(any(String.class), any(MenuItemRequest.class))).thenReturn(invalidValue);

        try {
            mvc.perform(put("/cafe/menu/update/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(Util.mapToJson(bodyContent)));
        }catch (MenuItemValueInvalid e) {
            Assertions.assertEquals(MenuItemValueInvalid.class, e.getClass());
        }
    }

    @Test
    void testDeleteMenuItem() throws Exception {
        mvc.perform(delete("/cafe/menu/delete/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(handler().methodName("deleteMenuItem"));

        verify(service, atLeastOnce()).delete(any(String.class));
    }

    @Test
    void testMenuItemValueInvalid() {

        String invalidValueCategoryName = "price";
        String expectedMessage = "The value of price is invalid";
        MenuItemValueInvalid exception = new MenuItemValueInvalid(invalidValueCategoryName);
        assertEquals(expectedMessage, exception.getMessage());

    }

    @Test
    void testMenuItemValueEmpty() {

        String emptyValueCategoryName = "name";
        String expectedMessage = "The menu item name request can't be empty";
        MenuItemValueEmpty exception = new MenuItemValueEmpty(emptyValueCategoryName);
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void testBadRequest() {

        String expectedMessage = "400 Bad Request";
        BadRequest exception = new BadRequest();
        assertEquals(expectedMessage, exception.getMessage());
    }
}