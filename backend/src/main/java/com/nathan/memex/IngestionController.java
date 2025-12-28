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
    public String upload(@RequestParam("files") List<MultipartFile> files) {
        try {
            if (files == null || files.isEmpty()) {
                return "Error: No files provided";
            }
            int processedCount = 0;
            for (MultipartFile file : files) {
                if (file.isEmpty()) {
                    continue;
                }
                ingestionService.startIngestion(file);
                processedCount++;
            }
            return "Uploaded " + processedCount + " file(s) successfully";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    @GetMapping("/api/search")
    public List<MemexDocument> searchDocs(@RequestParam("q") String query) {
        return ingestionService.search(query);
    }
}