package com.nathan.memex;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemexRepository extends ElasticsearchRepository<MemexDocument, String> {}