package in.ashar.my_edms.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import in.ashar.my_edms.entity.Metadata;
import in.ashar.my_edms.repository.MetadataRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

@Service
public class MetadataService {

    private final MetadataRepository metadataRepository;
    private final ElasticsearchClient elasticsearchClient;

    public MetadataService(MetadataRepository metadataRepository, ElasticsearchClient elasticsearchClient) {
        this.metadataRepository = metadataRepository;
        this.elasticsearchClient = elasticsearchClient;
    }

    @Transactional
    public Metadata saveMetadata(String docId, Metadata metadata) {
        metadata.setDocId(docId);
        Metadata saved = metadataRepository.save(metadata);

        // Index in Elasticsearch
        try {
            elasticsearchClient.index(i -> i
                    .index("metadata")
                    .id(docId)
                    .document(metadata)
            );
        } catch (IOException e) {
            System.err.println("⚠️ Failed to index document in Elasticsearch: " + e.getMessage());
        }

        return saved;
    }

    public Metadata getMetadata(String docId) {
        return metadataRepository.findById(docId).orElse(null);
    }

    public List<Metadata> getAllMetadata() {
        return metadataRepository.findAll();
    }
}
