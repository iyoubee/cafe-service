package id.ac.ui.cs.advprog.cafeservice.Ports;

import id.ac.ui.cs.advprog.cafeservice.Models.MenuItem;

import java.util.HashMap;
import java.util.Map;

public class MenuItemRepository implements MenuItemRepositoryInterface{
    Map<String, MenuItem> menuItemMap = new HashMap<>();

    @Override
    public MenuItem getOneMenuItem() {
        return null;
    }

    @Override
    public Map<String, MenuItem> getAllMenuItem() {
        return null;
    }
}
