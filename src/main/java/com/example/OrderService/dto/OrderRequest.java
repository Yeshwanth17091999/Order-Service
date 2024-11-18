package com.example.OrderService.dto;

import java.util.List;

import lombok.Data;

@Data
public class OrderRequest {

    private String customerName;
    private List<OrderItemRequest> items;

    // Getters and Setters
}
