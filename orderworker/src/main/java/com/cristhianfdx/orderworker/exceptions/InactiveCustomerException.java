package com.cristhianfdx.orderworker.exceptions;

public class InactiveCustomerException extends RuntimeException {
    public InactiveCustomerException(String customerId) {
        super("Inactive customer with ID: " + customerId);
    }
}
