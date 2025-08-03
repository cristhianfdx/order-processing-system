package com.cristhianfdx.orderworker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class OrderWorkerApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderWorkerApplication.class, args);
    }
}
