package id.ac.ui.cs.advprog.cafeservice;

import id.ac.ui.cs.advprog.cafeservice.validator.MenuItemValidator;
import id.ac.ui.cs.advprog.cafeservice.validator.OrderValidator;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

@SpringBootTest
class CafeServiceApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void testRestTemplateBean() {
        RestTemplateBuilder builderMock = mock(RestTemplateBuilder.class);
        RestTemplate restTemplateMock = mock(RestTemplate.class);
        Mockito.when(builderMock.build()).thenReturn(restTemplateMock);

        CafeServiceApplication cafeServiceApplication = new CafeServiceApplication();
        RestTemplate restTemplate = cafeServiceApplication.restTemplate(builderMock);

        assertNotNull(restTemplate);
    }

    @Test
    void testRestTemplateBuilderBean() {
        CafeServiceApplication cafeServiceApplication = new CafeServiceApplication();
        RestTemplateBuilder restTemplateBuilder = cafeServiceApplication.restTemplateBuilder();

        assertNotNull(restTemplateBuilder);
    }

    @Test
    void testMenuItemValidatorBean() {
        CafeServiceApplication cafeServiceApplication = new CafeServiceApplication();
        MenuItemValidator menuItemValidator = cafeServiceApplication.menuItemValidator();

        assertNotNull(menuItemValidator);
    }

    @Test
    void testOrderValidatorBean() {
        CafeServiceApplication cafeServiceApplication = new CafeServiceApplication();
        OrderValidator orderValidator = cafeServiceApplication.orderValidator();

        assertNotNull(orderValidator);
    }
}
