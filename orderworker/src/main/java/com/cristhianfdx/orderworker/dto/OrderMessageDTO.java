package com.cristhianfdx.orderworker.dto;

import lombok.Data;

import java.util.List;

@Data
public class OrderMessageDTO {
    private String orderId;
    private String customerId;
    private List<String> products;
}
