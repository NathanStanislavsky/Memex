package com.nathan.memex;

import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@Service
public class IngestionService {

    private final Tika tika;

    public IngestionService() {
        this.tika = new Tika();
    }

    public String extractContent(MultipartFile file) throws IOException {
        try {
            String type = tika.detect(file.getInputStream());
            System.out.println("Reading file of type: " + type);

            return tika.parseToString(file.getInputStream());
        } catch (Exception e) {
            throw new IOException("Tika failed to parse file", e);
        }
    }
}