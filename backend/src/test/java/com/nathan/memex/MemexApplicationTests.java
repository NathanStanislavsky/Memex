package com.nathan.memex;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;

@SpringBootTest
class MemexApplicationTests {

	// 1. Mock RabbitMQ
	@MockBean
	private ConnectionFactory connectionFactory;

	// 2. Mock Elasticsearch
	@MockBean(name = "elasticsearchClient")
	private Object elasticsearchClient;

	// 3. Mock S3 Client
	@MockBean(name = "s3Client")
	private Object s3Client;

	@Test
	void contextLoads() {
		
	}

}