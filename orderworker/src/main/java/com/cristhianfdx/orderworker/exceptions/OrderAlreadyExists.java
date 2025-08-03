package com.cristhianfdx.orderworker.exceptions;

public class OrderAlreadyExists extends RuntimeException {
    public OrderAlreadyExists(String orderId) {
        super("Order already exist with ID: " + orderId);
    }
}
