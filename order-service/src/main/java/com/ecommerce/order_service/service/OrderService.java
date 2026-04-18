package com.ecommerce.order_service.service;

import com.ecommerce.avro.OrderEvent;
import com.ecommerce.order_service.dto.Product;
import com.ecommerce.order_service.entity.Orders;
import com.ecommerce.order_service.event.OrderEvent_old;
import com.ecommerce.order_service.repo.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class OrderService {

    @Autowired
    private OrderRepository repository;

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Autowired
    private KafkaTemplate<String, OrderEvent> kafkaTemplate;

    public Orders placeOrder(Long productId, int quantity) {
        // 🔹 Call Product Service
        Product product = webClientBuilder.build()
                .get()
                .uri("http://product-service/products/" + productId)
                .retrieve()
                .bodyToMono(Product.class)
                .block();

        if (product == null) {
            throw new RuntimeException("Product not found");
        }

        // 🔹 Calculate price
        double totalPrice = product.getPrice() * quantity;

        // 🔹 Save order
        Orders order = new Orders();
        order.setProductId(productId);
        order.setQuantity(quantity);
        order.setTotalPrice(totalPrice);

        Orders savedOrder = repository.save(order);



        OrderEvent event = OrderEvent.newBuilder()
                .setOrderId(savedOrder.getId())
                .setProductId(productId)
                .setQuantity(quantity)
                .setStatus("CREATED")
                .build();



        System.out.println("Sending event to Kafka: {}" + event);
        kafkaTemplate.send("order-events", event)
                .addCallback(success -> System.out.println("✅ Sent: " + event),
                failure -> System.out.println("❌ Failed: " + failure.getMessage()));

        return savedOrder;
    }

}
