package com.example.OrderService.service;

import com.example.OrderService.dto.OrderRequest;
import com.example.OrderService.model.Order;
import com.example.OrderService.model.OrderItem;
import com.example.OrderService.model.Product;
import com.example.OrderService.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    private final WebClient.Builder webClientBuilder;

    public OrderService(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    public Mono<Product> getProductById(Long id) {
        String url = "http://localhost:8081/api/products/" + id;
        return webClientBuilder.build()
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(Product.class);
    }

    public Mono<Order> createOrder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setCustomerName(orderRequest.getCustomerName());
        order.setOrderDate(LocalDateTime.now());
        order.setOrderStatus("ORDER_PLACED");

        // Fetching product details for each order item
        List<Mono<OrderItem>> itemMonos = orderRequest.getItems().stream().map(item -> {
            return getProductById(item.getProductId())
                    .flatMap(product -> {
                        // Check stock availability
                        if (product.getStockQuantity() < item.getQuantity()) {
                            return Mono.error(new RuntimeException("Product out of stock"));
                        }
                        System.out.println("Hello in create order");
                        return updateProductStock(product.getId(), product.getStockQuantity() - item.getQuantity())
                                .flatMap(updatedProduct -> {
                                    // Create OrderItem after successfully updating stock
                                    return Mono.just(new OrderItem(item.getProductId(), product.getName(), item.getQuantity(), product.getPrice()));
                                });
                    });
        }).collect(Collectors.toList());
        
        System.out.println(itemMonos);

        // Combine all the OrderItems into a list
        return Mono.zip(itemMonos, orderItemsarr -> {
            List<OrderItem> items = List.of(orderItemsarr).stream()
                    .map(item -> (OrderItem) item)
                    .collect(Collectors.toList());
            order.setItems(items);
            return orderRepository.save(order);
        });
    }

    public Mono<Product> updateProductStock(Long productId, int newStockQuantity) {
        String url = "http://localhost:8081/api/products/" + productId + "/stock";
        
        return webClientBuilder.build()
                .put()
                .uri(url)
                .bodyValue(newStockQuantity) // Assuming the product service accepts the new stock quantity as the request body
                .retrieve()
          
                .bodyToMono(Product.class);
    }

	public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public void cancelOrder(Long id) {
        Order order = getOrderById(id);
        order.setOrderStatus("CANCELLED");
        orderRepository.save(order);
    }
}
