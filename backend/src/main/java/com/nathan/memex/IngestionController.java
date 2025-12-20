package com.nathan.memex;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class IngestionController {

    private final IngestionService ingestionService;

    public IngestionController(IngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @PostMapping("/api/upload")
    public String upload(@RequestParam("file") MultipartFile file) {
        try {
            String result = ingestionService.startIngestion(file);
            return result;
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @GetMapping("/api/search")
    public List<MemexDocument> searchDocs(@RequestParam("q") String query) {
        return ingestionService.search(query);
    }
}