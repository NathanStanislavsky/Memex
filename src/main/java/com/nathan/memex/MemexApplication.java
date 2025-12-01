package com.nathan.memex;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.AbstractElasticsearchTemplate;

@SpringBootApplication
public class MemexApplication {

    public static void main(String[] args) {
        SpringApplication.run(MemexApplication.class, args);
    }

    // This bean runs automatically when the application starts
    @Bean
    CommandLineRunner runner(ElasticsearchOperations esOps) {
        return args -> {
            System.out.println("----------------------------------------");
            System.out.println("Connecting to Elasticsearch...");
            
            // Ask the database for its version to prove we are connected
            String version = null;
            if (esOps instanceof AbstractElasticsearchTemplate) {
                version = ((AbstractElasticsearchTemplate) esOps).getClusterVersion();
            } else {
                throw new IllegalStateException("ElasticsearchOperations is not an instance of AbstractElasticsearchTemplate");
            }
            
            System.out.println("SUCCESS! Connected to Elasticsearch Version: " + version);
            System.out.println("----------------------------------------");
        };
    }
}