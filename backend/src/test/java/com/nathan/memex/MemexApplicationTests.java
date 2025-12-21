package com.nathan.memex;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import software.amazon.awssdk.services.s3.S3Client;

@SpringBootTest
class MemexApplicationTests {
    @MockBean
    private ConnectionFactory connectionFactory;

    @MockBean
    private ElasticsearchTemplate elasticsearchTemplate;

    @MockBean
    private S3Client s3Client;

    @Test
    void contextLoads() {

    }

}
