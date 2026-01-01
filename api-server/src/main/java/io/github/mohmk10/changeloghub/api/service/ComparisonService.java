package io.github.mohmk10.changeloghub.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mohmk10.changeloghub.api.dto.ComparisonRequest;
import io.github.mohmk10.changeloghub.api.dto.ComparisonResponse;
import io.github.mohmk10.changeloghub.api.entity.Comparison;
import io.github.mohmk10.changeloghub.api.entity.User;
import io.github.mohmk10.changeloghub.api.repository.ComparisonRepository;
import io.github.mohmk10.changeloghub.core.model.ApiSpec;
import io.github.mohmk10.changeloghub.core.model.Change;
import io.github.mohmk10.changeloghub.core.model.Changelog;
import io.github.mohmk10.changeloghub.core.model.Severity;
import io.github.mohmk10.changeloghub.core.service.AnalysisService;
import io.github.mohmk10.changeloghub.core.service.impl.DefaultAnalysisService;
import io.github.mohmk10.changeloghub.parser.openapi.OpenApiParser;
import io.github.mohmk10.changeloghub.parser.openapi.impl.DefaultOpenApiParser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ComparisonService {

    private final ComparisonRepository comparisonRepository;
    private final ObjectMapper objectMapper;
    private final OpenApiParser openApiParser;
    private final AnalysisService analysisService;

    public ComparisonService(ComparisonRepository comparisonRepository, ObjectMapper objectMapper) {
        this.comparisonRepository = comparisonRepository;
        this.objectMapper = objectMapper;
        this.openApiParser = new DefaultOpenApiParser();
        this.analysisService = new DefaultAnalysisService();
    }

    public ComparisonResponse compare(ComparisonRequest request) {
        ApiSpec oldSpec = openApiParser.parse(request.getOldSpec());
        ApiSpec newSpec = openApiParser.parse(request.getNewSpec());
        Changelog changelog = analysisService.analyze(oldSpec, newSpec);
        return buildResponse(changelog, request.getOldSpecName(), request.getNewSpecName(), request.getFormat());
    }

    @Transactional
    public ComparisonResponse compareAndSave(ComparisonRequest request, User user) {
        ComparisonResponse response = compare(request);
        saveComparison(response, user, request.getFormat());
        return response;
    }

    public List<ComparisonResponse> getUserComparisons(User user) {
        List<Comparison> comparisons = comparisonRepository.findByUserOrderByCreatedAtDesc(user);
        return comparisons.stream().map(this::toResponse).toList();
    }

    public ComparisonResponse getComparisonById(UUID id) {
        return comparisonRepository.findById(id)
                .map(this::toResponse)
                .orElse(null);
    }

    private void saveComparison(ComparisonResponse response, User user, String format) {
        Comparison comparison = new Comparison();
        comparison.setUser(user);
        comparison.setOldSpecName(response.getOldSpecName());
        comparison.setNewSpecName(response.getNewSpecName());
        comparison.setOldSpecFormat(format);
        comparison.setNewSpecFormat(format);
        comparison.setBreakingCount(response.getBreakingCount());
        comparison.setDangerousCount(response.getDangerousCount());
        comparison.setWarningCount(response.getWarningCount());
        comparison.setInfoCount(response.getInfoCount());
        comparison.setRiskScore(response.getRiskScore());
        comparison.setStabilityGrade(response.getStabilityGrade());
        comparison.setSemverRecommendation(response.getSemverRecommendation());

        try {
            comparison.setChangesJson(objectMapper.writeValueAsString(response.getChanges()));
        } catch (JsonProcessingException e) {
            comparison.setChangesJson("[]");
        }

        Comparison saved = comparisonRepository.save(comparison);
        response.setId(saved.getId());
        response.setCreatedAt(saved.getCreatedAt());
    }

    private ComparisonResponse buildResponse(Changelog changelog, String oldSpecName, String newSpecName, String format) {
        ComparisonResponse response = new ComparisonResponse();
        response.setOldSpecName(oldSpecName != null ? oldSpecName : "old-spec");
        response.setNewSpecName(newSpecName != null ? newSpecName : "new-spec");
        response.setFormat(format != null ? format : "openapi");

        int breaking = 0, dangerous = 0, warning = 0, info = 0;
        List<ComparisonResponse.ChangeDto> changes = new ArrayList<>();

        for (Change change : changelog.getChanges()) {
            ComparisonResponse.ChangeDto dto = new ComparisonResponse.ChangeDto();
            dto.setType(change.getType() != null ? change.getType().name() : "UNKNOWN");
            dto.setSeverity(change.getSeverity() != null ? change.getSeverity().name() : "INFO");
            dto.setPath(change.getPath());
            dto.setDescription(change.getDescription());
            dto.setOldValue(change.getOldValue() != null ? change.getOldValue().toString() : null);
            dto.setNewValue(change.getNewValue() != null ? change.getNewValue().toString() : null);
            changes.add(dto);

            if (change.getSeverity() != null) {
                switch (change.getSeverity()) {
                    case BREAKING -> breaking++;
                    case DANGEROUS -> dangerous++;
                    case WARNING -> warning++;
                    case INFO -> info++;
                }
            }
        }

        response.setChanges(changes);
        response.setBreakingCount(breaking);
        response.setDangerousCount(dangerous);
        response.setWarningCount(warning);
        response.setInfoCount(info);

        if (changelog.getRiskAssessment() != null) {
            response.setRiskScore(changelog.getRiskAssessment().getOverallScore());
            response.setStabilityGrade(calculateStabilityGrade(changelog.getRiskAssessment().getOverallScore()));
            response.setSemverRecommendation(changelog.getRiskAssessment().getSemverRecommendation());
        } else {
            int riskScore = calculateRiskScore(breaking, dangerous, warning);
            response.setRiskScore(riskScore);
            response.setStabilityGrade(calculateStabilityGrade(riskScore));
            response.setSemverRecommendation(calculateSemverRecommendation(breaking, dangerous, warning, info));
        }

        return response;
    }

    private int calculateRiskScore(int breaking, int dangerous, int warning) {
        return Math.min(100, breaking * 25 + dangerous * 10 + warning * 3);
    }

    private String calculateStabilityGrade(int riskScore) {
        if (riskScore == 0) return "A+";
        if (riskScore <= 10) return "A";
        if (riskScore <= 25) return "B";
        if (riskScore <= 50) return "C";
        if (riskScore <= 75) return "D";
        return "F";
    }

    private String calculateSemverRecommendation(int breaking, int dangerous, int warning, int info) {
        if (breaking > 0) return "MAJOR";
        if (dangerous > 0 || warning > 0) return "MINOR";
        if (info > 0) return "PATCH";
        return "NONE";
    }

    private ComparisonResponse toResponse(Comparison comparison) {
        ComparisonResponse response = new ComparisonResponse();
        response.setId(comparison.getId());
        response.setOldSpecName(comparison.getOldSpecName());
        response.setNewSpecName(comparison.getNewSpecName());
        response.setFormat(comparison.getOldSpecFormat());
        response.setBreakingCount(comparison.getBreakingCount());
        response.setDangerousCount(comparison.getDangerousCount());
        response.setWarningCount(comparison.getWarningCount());
        response.setInfoCount(comparison.getInfoCount());
        response.setRiskScore(comparison.getRiskScore());
        response.setStabilityGrade(comparison.getStabilityGrade());
        response.setSemverRecommendation(comparison.getSemverRecommendation());
        response.setCreatedAt(comparison.getCreatedAt());

        try {
            if (comparison.getChangesJson() != null) {
                List<ComparisonResponse.ChangeDto> changes = objectMapper.readValue(
                        comparison.getChangesJson(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, ComparisonResponse.ChangeDto.class)
                );
                response.setChanges(changes);
            }
        } catch (JsonProcessingException e) {
            response.setChanges(new ArrayList<>());
        }

        return response;
    }
}
