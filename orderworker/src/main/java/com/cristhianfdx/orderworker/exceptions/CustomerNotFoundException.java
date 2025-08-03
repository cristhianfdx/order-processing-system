package com.cristhianfdx.orderworker.exceptions;

public class CustomerNotFoundException extends RuntimeException {
    public CustomerNotFoundException(String customerId) {
        super("Customer not found with ID: " + customerId);
    }
}
