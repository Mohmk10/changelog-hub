package io.github.mohmk10.changeloghub.api.controller;

import io.github.mohmk10.changeloghub.api.dto.ComparisonRequest;
import io.github.mohmk10.changeloghub.api.dto.ComparisonResponse;
import io.github.mohmk10.changeloghub.api.entity.User;
import io.github.mohmk10.changeloghub.api.service.ComparisonService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/comparisons")
public class ComparisonController {

    private final ComparisonService comparisonService;

    public ComparisonController(ComparisonService comparisonService) {
        this.comparisonService = comparisonService;
    }

    @PostMapping
    public ResponseEntity<ComparisonResponse> createComparison(
            @Valid @RequestBody ComparisonRequest request,
            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        ComparisonResponse response = comparisonService.compareAndSave(request, user);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/anonymous")
    public ResponseEntity<ComparisonResponse> createAnonymousComparison(
            @Valid @RequestBody ComparisonRequest request) {
        ComparisonResponse response = comparisonService.compare(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ComparisonResponse>> getUserComparisons(
            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<ComparisonResponse> comparisons = comparisonService.getUserComparisons(user);
        return ResponseEntity.ok(comparisons);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ComparisonResponse> getComparison(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user) {
        ComparisonResponse response = comparisonService.getComparisonById(id);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }
}
