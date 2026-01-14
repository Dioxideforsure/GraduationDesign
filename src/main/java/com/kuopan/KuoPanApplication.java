package com.kuopan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableAsync
@SpringBootApplication(scanBasePackages = {"com.kuopan"})
@EnableTransactionManagement
@EnableScheduling
public class KuoPanApplication {
    public static void main(String[] args) {
        SpringApplication.run(KuoPanApplication.class, args);
    }
}
