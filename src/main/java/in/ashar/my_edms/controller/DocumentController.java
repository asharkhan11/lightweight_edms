package in.ashar.my_edms.controller;


import in.ashar.my_edms.entity.Metadata;
import in.ashar.my_edms.service.DocumentService;
import in.ashar.my_edms.service.FileTextExtractorService;
import in.ashar.my_edms.service.MetadataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;
    private final FileTextExtractorService textExtractor;
    private final MetadataService metadataService;


    // Upload file
    @PostMapping("/upload")
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file) {
        try {
            // 1️⃣ Upload to MinIO
            String docId = documentService.uploadFile(file);

            // 2️⃣ Extract content using Apache Tika
            Metadata extracted = textExtractor.extractText(file, docId);

            // 3️⃣ Save metadata (MySQL + Elasticsearch)
            metadataService.saveMetadata(docId, extracted);

            return ResponseEntity.ok("Uploaded, analyzed, and indexed with ID: " + docId);

        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Upload failed: " + e.getMessage());
        }
    }


    // Download file
    @GetMapping("/{id}")
    public ResponseEntity<Resource> download(@PathVariable String id) {
        try {
            Resource resource = documentService.downloadFile(id);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Delete file
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable String id) {
        try {
            documentService.deleteFile(id);
            return ResponseEntity.ok("Deleted: " + id);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Delete failed: " + e.getMessage());
        }
    }

    // List all documents (metadata only)
    @GetMapping
    public ResponseEntity<List<String>> listAll() {
        return ResponseEntity.ok(documentService.listDocuments());
    }
}
