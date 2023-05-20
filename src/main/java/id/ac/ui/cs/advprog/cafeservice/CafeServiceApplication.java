package id.ac.ui.cs.advprog.cafeservice;

import id.ac.ui.cs.advprog.cafeservice.validator.MenuItemValidator;
import id.ac.ui.cs.advprog.cafeservice.validator.OrderValidator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class CafeServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CafeServiceApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    @Bean
    public RestTemplateBuilder restTemplateBuilder(){
        return new RestTemplateBuilder();
    }

    @Bean
    public MenuItemValidator menuItemValidator() {
        return new MenuItemValidator();
    }

    @Bean
    public OrderValidator orderValidator() {
        return new OrderValidator();
    }

}
