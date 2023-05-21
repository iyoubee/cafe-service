package id.ac.ui.cs.advprog.cafeservice.pattern.strategy.create;

import id.ac.ui.cs.advprog.cafeservice.model.order.OrderDetails;

public interface CreateStrategy {
    OrderDetails create();
}
