package id.ac.ui.cs.advprog.cafeservice.Models;

import java.util.UUID;

public class MenuItem {
    private String id;
    private String name;
    private Integer price;
    private Integer stock;

    public MenuItem(String name, Integer price, Integer stock) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.price = price;
        this.stock = stock;
    }
}
