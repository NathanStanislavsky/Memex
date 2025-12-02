package com.nathan.memex;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "memex-docs")
public class MemexDocument {

    @Id
    private String id;

    @Field(type = FieldType.Text)
    private String content;

    public MemexDocument(String id, String content) {
        this.id = id;
        this.content = content;
    }

    public String getId() { 
        return id; 
    }

    public String getContent() { 
        return content; 
    }
}