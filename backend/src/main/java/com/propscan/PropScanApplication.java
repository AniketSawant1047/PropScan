package com.propscan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class PropScanApplication {

    public static void main(String[] args) {
        SpringApplication.run(PropScanApplication.class, args);
    }
}
