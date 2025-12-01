package com.nathan.memex;

import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

@Service
public class IngestionService {

    private final Tika tika;
    private final MemexRepository repository;

    public IngestionService(MemexRepository repository) {
        this.tika = new Tika();
        this.repository = repository;
    }

    public String extractContent(MultipartFile file) throws IOException {
        try {
            String content = tika.parseToString(file.getInputStream());

            MemexDocument doc = new MemexDocument(file.getOriginalFilename(), content);

            repository.save(doc);
            
            System.out.println("Saved document: " + doc.getId());

            return content;
        } catch (Exception e) {
            throw new IOException("Failed to process file", e);
        }
    }

    public List<MemexDocument> search(String keyword) {
        System.out.println("Searching for: " + keyword);
        return repository.findByContent(keyword);
    }
}