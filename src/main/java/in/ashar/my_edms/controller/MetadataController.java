package in.ashar.my_edms.controller;


import in.ashar.my_edms.entity.Metadata;
import in.ashar.my_edms.service.MetadataService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/metadata")
public class MetadataController {

    private final MetadataService metadataService;

    public MetadataController(MetadataService metadataService) {
        this.metadataService = metadataService;
    }

    // Save or update metadata
    @PostMapping("/{docId}")
    public ResponseEntity<Metadata> saveMetadata(@PathVariable String docId, @RequestBody Metadata metadata) {
        return ResponseEntity.ok(metadataService.saveMetadata(docId, metadata));
    }

    // Get metadata for a document
    @GetMapping("/{docId}")
    public ResponseEntity<Metadata> getMetadata(@PathVariable String docId) {
        Metadata metadata = metadataService.getMetadata(docId);
        if (metadata == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(metadata);
    }

    // List all metadata
    @GetMapping
    public ResponseEntity<List<Metadata>> getAllMetadata() {
        return ResponseEntity.ok(metadataService.getAllMetadata());
    }
}
