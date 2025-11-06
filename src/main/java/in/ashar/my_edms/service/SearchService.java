package in.ashar.my_edms.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import in.ashar.my_edms.entity.Metadata;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final ElasticsearchClient elasticsearchClient;


    public List<Metadata> search(String keyword) {
        try {
            SearchRequest request = SearchRequest.of(s -> s
                    .index("metadata") // your index name
                    .query(q -> q
                            .multiMatch(m -> m
                                    .query(keyword)
                                    .fields("docId","title", "description", "tags")
                            )
                    )
            );

            SearchResponse<Metadata> response = elasticsearchClient.search(request, Metadata.class);

            return response.hits().hits().stream()
                    .map(Hit::source)
                    .collect(Collectors.toList());

        } catch (IOException e) {
            throw new RuntimeException("Error searching Elasticsearch: " + e.getMessage(), e);
        }
    }
}
