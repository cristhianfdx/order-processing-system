package com.cristhianfdx.orderworker.dto;

import lombok.Data;

@Data
public class ProductDTO {
    private String id;
    private String name;
    private String description;
    private double price;
}
