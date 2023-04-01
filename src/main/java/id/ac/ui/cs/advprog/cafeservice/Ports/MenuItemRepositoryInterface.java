package id.ac.ui.cs.advprog.cafeservice.Ports;
import id.ac.ui.cs.advprog.cafeservice.Models.MenuItem;

import java.util.Map;

public interface MenuItemRepositoryInterface {
    public MenuItem getOneMenuItem();
    public Map<String, MenuItem> getAllMenuItem();
}
