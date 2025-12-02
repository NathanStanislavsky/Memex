package com.nathan.memex;

import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MemexRepository extends ElasticsearchRepository<MemexDocument, String> {
    @Query("{\"match\": {\"content\": {\"query\": \"?0\", \"fuzziness\": \"AUTO\"}}}")
    List<MemexDocument> findByContent(String text);
}