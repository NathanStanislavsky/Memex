package com.nathan.memex;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.elasticsearch.client.RestHighLevelClient;
import software.amazon.awssdk.services.s3.S3Client;

@SpringBootTest
class MemexApplicationTests {

    // 1. Mock RabbitMQ
    @MockBean
    private ConnectionFactory connectionFactory;

    // 2. Mock Elasticsearch
    @MockBean
    private RestHighLevelClient restHighLevelClient;

    // 3. Mock S3 Client (MinIO)
    @MockBean
    private S3Client s3Client;

    @Test
    void contextLoads() {
		
    }

}
