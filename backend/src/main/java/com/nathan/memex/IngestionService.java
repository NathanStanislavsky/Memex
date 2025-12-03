package com.nathan.memex;

import org.apache.tika.Tika;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;
import java.util.List;

@Service
public class IngestionService {

    private final RabbitTemplate rabbitTemplate;
    private final MemexRepository repository;
    private final S3Client s3Client;
    private final Tika tika;

    private static final String BUCKET_NAME = "memex-files";

    public IngestionService(RabbitTemplate rabbitTemplate, MemexRepository repository, S3Client s3Client) {
        this.rabbitTemplate = rabbitTemplate;
        this.repository = repository;
        this.s3Client = s3Client;
        this.tika = new Tika();
    }

    public String startIngestion(MultipartFile file) throws Exception {
        String filename = file.getOriginalFilename();
        
        System.out.println("API: Uploading " + filename + " to S3...");

        PutObjectRequest putReq = PutObjectRequest.builder()
                .bucket(BUCKET_NAME)
                .key(filename)
                .build();

        s3Client.putObject(putReq, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        
        System.out.println("API: Upload Complete. Sending message to Queue.");

        rabbitTemplate.convertAndSend("memex-ingestion-queue", filename);
        
        return "PROCESSING_STARTED";
    }

    @RabbitListener(queues = "memex-ingestion-queue")
    public void consumeMessage(String filename) {
        System.out.println("WORKER: Received task for " + filename);

        try {
            GetObjectRequest getReq = GetObjectRequest.builder()
                    .bucket(BUCKET_NAME)
                    .key(filename)
                    .build();

            try (InputStream s3Stream = s3Client.getObject(getReq)) {
                
                System.out.println("WORKER: Downloaded stream. Parsing PDF...");
                
                String content = tika.parseToString(s3Stream);

                MemexDocument doc = new MemexDocument(filename, content);
                repository.save(doc);
                
                System.out.println("WORKER: Success! Indexed " + filename);
            }

        } catch (Exception e) {
            System.err.println("WORKER FAILED: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<MemexDocument> search(String keyword) {
        return repository.findByContent(keyword);
    }
}