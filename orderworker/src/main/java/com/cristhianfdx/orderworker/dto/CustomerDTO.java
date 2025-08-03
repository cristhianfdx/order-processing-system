package com.cristhianfdx.orderworker.dto;

import lombok.Data;

@Data
public class CustomerDTO {
    private String id;
    private String name;
    private String email;
    private CustomerStatusEnum status;
}
