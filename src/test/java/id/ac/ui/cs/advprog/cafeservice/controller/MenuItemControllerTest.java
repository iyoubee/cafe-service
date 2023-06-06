package id.ac.ui.cs.advprog.cafeservice.controller;

import id.ac.ui.cs.advprog.cafeservice.Util;
import id.ac.ui.cs.advprog.cafeservice.dto.MenuItemRequest;
import id.ac.ui.cs.advprog.cafeservice.exceptions.BadRequest;
import id.ac.ui.cs.advprog.cafeservice.exceptions.MenuItemDoesNotExistException;
import id.ac.ui.cs.advprog.cafeservice.exceptions.MenuItemValueEmpty;
import id.ac.ui.cs.advprog.cafeservice.exceptions.MenuItemValueInvalid;
import id.ac.ui.cs.advprog.cafeservice.model.menu.MenuItem;
import id.ac.ui.cs.advprog.cafeservice.service.MenuItemServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
            .id("1")
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
    void testInvalidPriceAddMenuItem() throws Exception {
        when(service.create(any(MenuItemRequest.class))).thenReturn(invalidValue);

        bodyContent = new Object() {
            public final String name = "Indomie";

            public final int price = -1;

            public final int stock = 100;
        };

        mvc.perform(post("/cafe/menu/create")
            .contentType(MediaType.APPLICATION_JSON)
            .content(Util.mapToJson(bodyContent)))
                .andExpect(status().isBadRequest());

    }

    @Test
    void testNameEmptyAddMenuItem() throws Exception {
        when(service.create(any(MenuItemRequest.class))).thenReturn(emptyName);

        bodyContent = new Object() {
            public final String name = "";

            public final int price = 1;

            public final int stock = 100;
        };

        mvc.perform(post("/cafe/menu/create")
            .contentType(MediaType.APPLICATION_JSON)
            .content(Util.mapToJson(bodyContent)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testInvalidStockAddMenuItem() throws Exception {
        when(service.create(any(MenuItemRequest.class))).thenReturn(emptyName);

        bodyContent = new Object() {
            public final String name = "Indomie";

            public final int price = 5;

            public final int stock = -10;
        };

        mvc.perform(post("/cafe/menu/create")
            .contentType(MediaType.APPLICATION_JSON)
            .content(Util.mapToJson(bodyContent)))
                .andExpect(status().isBadRequest());
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
    void testInvalidPricePutMenuItem() throws Exception {
        when(service.create(any(MenuItemRequest.class))).thenReturn(invalidValue);

        bodyContent = new Object() {
            public final String name = "Indomie";

            public final int price = -1;

            public final int stock = 100;
        };

        mvc.perform(put("/cafe/menu/update/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(Util.mapToJson(bodyContent)))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @MethodSource("invalidPutMenuItemRequests")
    void testPutInvalidMenuItem(MenuItemRequest request) throws Exception {
        when(service.update(anyString(), any(MenuItemRequest.class))).thenReturn(invalidValue);

        mvc.perform(put("/cafe/menu/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(Util.mapToJson(request)))
                .andExpect(status().isBadRequest());

    }

    private static Stream<Arguments> invalidPutMenuItemRequests() {
        return Stream.of(
                Arguments.of(new MenuItemRequest("Indomie", 10000000, 100)),
                Arguments.of(new MenuItemRequest("IndomieIndomieIndomieIndomieIndomieIndomie", 10000, 100)),
                Arguments.of(new MenuItemRequest(" ", 10000, 100))
        );
    }

    @Test
    void testStringPriceMenuItem() throws Exception {
        when(service.create(any(MenuItemRequest.class))).thenReturn(invalidValue);

        bodyContent = new Object() {
            public final String name = "Indomie";

            public final String price = "String";

            public final int stock = 100;
        };

        mvc.perform(post("/cafe/menu/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(Util.mapToJson(bodyContent)))
                .andExpect(status().isBadRequest())
                .andExpect(handler().methodName("addMenuItem"))
                .andExpect(jsonPath("$.message").value("Invalid request payload"));;
    }

    @Test
    void testNameEmptyPutMenuItem() throws Exception {
        when(service.create(any(MenuItemRequest.class))).thenReturn(emptyName);

        bodyContent = new Object() {
            public final String name = "";

            public final int price = 1;

            public final int stock = 100;
        };

        mvc.perform(put("/cafe/menu/update/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(Util.mapToJson(bodyContent)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testInvalidStockPutMenuItem() throws Exception {
        when(service.create(any(MenuItemRequest.class))).thenReturn(emptyName);

        bodyContent = new Object() {
            public final String name = "Indomie";

            public final int price = 5;

            public final int stock = -10;
        };

        mvc.perform(put("/cafe/menu/update/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(Util.mapToJson(bodyContent)))
                .andExpect(status().isBadRequest());
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

    @Test
    void testPutMenuItemShouldThrowMenuItemDoesntExistException() throws Exception {
        when(service.update(anyString(), any(MenuItemRequest.class))).thenThrow(MenuItemDoesNotExistException.class);

        mvc.perform(put("/cafe/menu/update/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(Util.mapToJson(bodyContent)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateMenuItemWithNullValueShouldThrowBadRequest() throws Exception {
        when(service.create(any(MenuItemRequest.class))).thenThrow(BadRequest.class);

        bodyContent = new Object() {
            public final String name = null;

            public final int price = 5;

            public final int stock = 10;
        };

        mvc.perform(post("/cafe/menu/create")
            .contentType(MediaType.APPLICATION_JSON)
            .content(Util.mapToJson(bodyContent)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetMenuItemShouldThrowException() throws Exception {
        when(service.findById(anyString())).thenThrow(MenuItemDoesNotExistException.class);

        mvc.perform(get("/cafe/menu/id/1")
            .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testNullPriceAddMenuItem() throws Exception {
        bodyContent = new Object() {
            public final String name = "Example Name";
            public final Integer price = null;
            public final Integer stock = 100;
        };

        mvc.perform(post("/cafe/menu/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(Util.mapToJson(bodyContent)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testNullStockAddMenuItem() throws Exception {
        bodyContent = new Object() {
            public final String name = "Example Name";
            public final Integer price = 10;
            public final Integer stock = null;
        };

        mvc.perform(post("/cafe/menu/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(Util.mapToJson(bodyContent)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testNullValuesAddMenuItem() throws Exception {
        bodyContent = new Object() {
            public final String name = null;
            public final Integer price = 1;
            public final Integer stock = null;
        };

        mvc.perform(post("/cafe/menu/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(Util.mapToJson(bodyContent)))
                .andExpect(status().isBadRequest());
    }

}