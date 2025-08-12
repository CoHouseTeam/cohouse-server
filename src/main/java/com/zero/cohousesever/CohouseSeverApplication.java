package com.zero.cohousesever;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class CohouseSeverApplication {

    public static void main(String[] args) {
        SpringApplication.run(CohouseSeverApplication.class, args);
    }

}
