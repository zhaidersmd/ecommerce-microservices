package com.ecommerce.inventory_service.consumer;


import com.ecommerce.avro.OrderEvent;
import com.ecommerce.inventory_service.entity.Inventory;
import com.ecommerce.inventory_service.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class InventoryConsumer {

    @Autowired
    private InventoryRepository repository;

    @KafkaListener(topics = "order-events", groupId = "inventory-group")
    public void consume(OrderEvent event) {

        System.out.println("🔥 Received: " + event);
        Inventory inventory = repository.findById(event.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        inventory.setQuantity(inventory.getQuantity() - event.getQuantity());

        repository.save(inventory);

        System.out.println("Inventory updated for product: " + event.getProductId());
    }
}
