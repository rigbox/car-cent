package com.rigsec.crent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(value = {"com.rigsec"})
public class CarRentApplication {
    public static void main(String[] args) {
        SpringApplication.run(CarRentApplication.class, args);
    }
}
