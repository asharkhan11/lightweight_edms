package in.ashar.my_edms.controller;


import in.ashar.my_edms.entity.Metadata;
import in.ashar.my_edms.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    // Search documents by keyword
    @GetMapping
    public ResponseEntity<List<Metadata>> search(@RequestParam String q) {
        return ResponseEntity.ok(searchService.search(q));
    }
}
