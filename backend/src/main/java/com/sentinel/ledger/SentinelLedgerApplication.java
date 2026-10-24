package com.sentinel.ledger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class SentinelLedgerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SentinelLedgerApplication.class, args);
    }
}
