package com.example.OrderService.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
	public class OrderItem {
	
		@Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;

	    private Long productId;
	    private String productName;
	    private int quantity;
	    private double price;
		public OrderItem( Long productId, String productName, int quantity, double price) {
			super();
			
			this.productId = productId;
			this.productName = productName;
			this.quantity = quantity;
			this.price = price;
		}
		public OrderItem() {
			super();
		}
		
		

	    // Getters and Setters
	    
	    
	}

