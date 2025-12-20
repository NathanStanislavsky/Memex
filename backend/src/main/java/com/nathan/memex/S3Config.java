package com.nathan.memex;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Configuration
public class S3Config {

    @Value("${S3_ENDPOINT:http://localhost:9000}")
    private String s3Endpoint;

    @Value("${S3_ACCESS_KEY:admin}")
    private String s3AccessKey;

    @Value("${S3_SECRET_KEY:password}")
    private String s3SecretKey;

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.US_EAST_1)
                .endpointOverride(URI.create(s3Endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(s3AccessKey, s3SecretKey)
                ))
                .forcePathStyle(true)
                .build();
    }
}