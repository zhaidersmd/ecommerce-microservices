package com.ecommerce.order_service.controller;

import com.ecommerce.order_service.entity.Orders;
import com.ecommerce.order_service.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService service;

    @PostMapping
    public Orders placeOrder(@RequestParam Long productId,
                             @RequestParam int quantity) {
        return service.placeOrder(productId, quantity);
    }
}
