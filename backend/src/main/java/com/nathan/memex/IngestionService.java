package com.nathan.memex;

import org.apache.tika.Tika;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class IngestionService {

    private final RabbitTemplate rabbitTemplate;
    private final MemexRepository repository;
    private final Tika tika;

    public IngestionService(RabbitTemplate rabbitTemplate, MemexRepository repository) {
        this.rabbitTemplate = rabbitTemplate;
        this.repository = repository;
        this.tika = new Tika();
    }

    public String startIngestion(MultipartFile file) {
        String filename = file.getOriginalFilename();
        
        System.out.println("API: Received request for " + filename);
        
        rabbitTemplate.convertAndSend("memex-ingestion-queue", filename);
        
        return "PROCESSING_STARTED";
    }

    @RabbitListener(queues = "memex-ingestion-queue")
    public void consumeMessage(String filename) {
        System.out.println("WORKER: Picked up job for " + filename);

        try {
            Thread.sleep(5000);
            System.out.println("WORKER: Finished processing " + filename);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public String extractContent(MultipartFile file) throws IOException {
        try {
            String content = tika.parseToString(file.getInputStream());

            MemexDocument doc = new MemexDocument(file.getOriginalFilename(), content);
            repository.save(doc);

            return content;
        } catch (Exception e) {
            throw new IOException("Failed to process file", e);
        }
    }

    public List<MemexDocument> search(String keyword) {
        return repository.findByContent(keyword);
    }
}