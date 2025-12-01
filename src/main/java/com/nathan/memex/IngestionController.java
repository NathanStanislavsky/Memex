package com.nathan.memex;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class IngestionController {

    private final IngestionService ingestionService;

    public IngestionController(IngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @PostMapping("/api/upload")
    public String upload(@RequestParam("file") MultipartFile file) {
        try {
            String content = ingestionService.extractContent(file);
            
            return "SUCCESS! Read " + content.length() + " characters.\nPreview:\n" 
                   + content.substring(0, Math.min(content.length(), 200)) + "...";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}