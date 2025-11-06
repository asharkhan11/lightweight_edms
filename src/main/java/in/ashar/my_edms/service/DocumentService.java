package in.ashar.my_edms.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import in.ashar.my_edms.repository.MetadataRepository;
import io.minio.*;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {

    private final MinioClient minioClient;
    private final MetadataRepository metadataRepository;
    private final ElasticsearchClient elasticsearchClient;

    @Value("${minio.bucket-name}")
    private String bucketName;


    // Upload file to MinIO
    public String uploadFile(MultipartFile file) throws Exception {
        String objectName = UUID.randomUUID().toString();

        // Ensure bucket exists
        boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (!bucketExists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }

        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
        }

        return objectName;
    }

    // Download file
    public Resource downloadFile(String objectName) throws Exception {
        InputStream stream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build()
        );

        return new InputStreamResource(stream);
    }

    // Delete file
    public void deleteFile(String docId) throws Exception {
        // 1️⃣ Get metadata to find object name (same as docId)
        var metaOpt = metadataRepository.findById(docId);
        if (metaOpt.isEmpty()) {
            throw new RuntimeException("Document not found: " + docId);
        }

        // 2️⃣ Delete from MinIO
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(docId)
                            .build()
            );
            log.info("✅ Deleted file from MinIO: {}", docId);
        } catch (Exception e) {
            throw new RuntimeException("MinIO deletion failed: " + e.getMessage(), e);
        }

        // 3️⃣ Delete from MySQL
        metadataRepository.deleteById(docId);
        log.info("✅ Deleted metadata from MySQL: {}", docId);

        // 4️⃣ Delete from Elasticsearch
        try {
            elasticsearchClient.delete(d -> d.index("metadata").id(docId));
            log.info("✅ Deleted document from Elasticsearch: {}", docId);
        } catch (Exception e) {
            log.error("⚠️ Elasticsearch delete failed (might already be gone): {}", e.getMessage());
        }
    }

    // List all files (object names)
    public List<String> listDocuments() {
        List<String> objects = new ArrayList<>();
        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder().bucket(bucketName).build()
        );
        results.forEach(r -> {
            try {
                objects.add(r.get().objectName());
            } catch (Exception ignored) {}
        });
        return objects;
    }
}
