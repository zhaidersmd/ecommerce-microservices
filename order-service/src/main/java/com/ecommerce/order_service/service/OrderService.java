package com.ecommerce.order_service.service;

import com.ecommerce.avro.OrderEvent;
import com.ecommerce.order_service.dto.Product;
import com.ecommerce.order_service.entity.Orders;
import com.ecommerce.order_service.event.OrderEvent_old;
import com.ecommerce.order_service.repo.OrderRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @CircuitBreaker(name = "productService", fallbackMethod = "fallBackProduct")
    @Retry(name = "productService")
    public Orders placeOrder(Long productId, int quantity) {
        // 🔹 Call Product Service
        Product product = webClientBuilder.build()
                .get()
                .uri("http://product-service/products/" + productId)
                .retrieve()
                .bodyToMono(Product.class)
                .block();

        if (product == null) {
            logger.error("Product Not Found");
            throw new RuntimeException("Product not found");
        }

        logger.info("Product Fetched: {}" , product.getId());
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
        logger.info("Kafka Message sent as {}" , event);
        return savedOrder;
    }

    public Orders fallBackProduct(Long productId, int quantity, Throwable throwable) {

        Orders order = new Orders();
        order.setProductId(productId);
        logger.error("Fallback method triggered " , throwable);
        return order;
    }

}