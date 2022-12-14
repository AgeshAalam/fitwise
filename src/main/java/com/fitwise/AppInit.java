package com.fitwise;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
//@EnableScheduling
public class AppInit {
    /**
     * Main Method
     *
     * @param args
     */
    public static void main(final String[] args) {
        SpringApplication.run(AppInit.class);
    }
}