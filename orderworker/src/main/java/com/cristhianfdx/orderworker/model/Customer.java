package com.cristhianfdx.orderworker.model;

import lombok.Data;

@Data
public class Customer {
    private String customerId;
    private String fullName;
    private boolean active;
}
