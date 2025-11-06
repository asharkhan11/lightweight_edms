package in.ashar.my_edms.configuration;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.mapping.*;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import jakarta.annotation.PostConstruct;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.context.annotation.*;

import java.io.IOException;
import java.util.Map;

@Configuration
public class ElasticConfig {

    @Bean
    public ElasticsearchClient elasticsearchClient() {
        RestClient restClient = RestClient.builder(
                new HttpHost("localhost", 9200)
        ).build();

        RestClientTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        return new ElasticsearchClient(transport);
    }

    @Bean
    public IndexInitializer indexInitializer(ElasticsearchClient client) {
        return new IndexInitializer(client);
    }

    public static class IndexInitializer {
        private final ElasticsearchClient client;

        public IndexInitializer(ElasticsearchClient client) {
            this.client = client;
        }

        @PostConstruct
        public void createIndexIfMissing() throws IOException {
            String indexName = "metadata";

            boolean exists = client.indices()
                    .exists(e -> e.index(indexName))
                    .value();

            if (!exists) {

                client.indices().create(c -> c
                        .index(indexName)
                        .mappings(m -> m.properties(Map.of(
                                "docId", Property.of(p -> p.keyword(KeywordProperty.of(k -> k))),
                                "title", Property.of(p -> p.text(TextProperty.of(t -> t))),
                                "description", Property.of(p -> p.text(TextProperty.of(t -> t))),
                                "tags", Property.of(p -> p.text(TextProperty.of(t -> t)))
                        )))
                );

                System.out.println("✅ Created Elasticsearch index: " + indexName);
            } else {
                System.out.println("ℹ️ Elasticsearch index already exists: " + indexName);
            }
        }
    }
}
