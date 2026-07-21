package com.example.penetration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class PenetrationApplication {

    public static void main(String[] args) {
        SpringApplication.run(PenetrationApplication.class, args);
    }

}
