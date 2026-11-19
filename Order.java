package com.ecommerce.oms;

public record Order(int orderId, String customerName, String productName,
                    int quantity, double unitPrice, String status) {
}
