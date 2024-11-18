package com.example.OrderService.dto;

import lombok.Data;

@Data
public class OrderItemRequest {

    private Long productId;
    private int quantity;

    // Getters and Setters
}
