package io.github.mohmk10.changeloghub.api.service;

import io.github.mohmk10.changeloghub.api.dto.AnalyticsResponse;
import io.github.mohmk10.changeloghub.api.entity.Comparison;
import io.github.mohmk10.changeloghub.api.entity.User;
import io.github.mohmk10.changeloghub.api.repository.ComparisonRepository;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnalyticsService {

    private final ComparisonRepository comparisonRepository;

    public AnalyticsService(ComparisonRepository comparisonRepository) {
        this.comparisonRepository = comparisonRepository;
    }

    public AnalyticsResponse getUserAnalytics(User user) {
        AnalyticsResponse response = new AnalyticsResponse();

        Long totalComparisons = comparisonRepository.countByUser(user);
        Long totalBreaking = comparisonRepository.sumBreakingCountByUser(user);
        Double avgRiskScore = comparisonRepository.avgRiskScoreByUser(user);

        response.setTotalComparisons(totalComparisons != null ? totalComparisons : 0L);
        response.setTotalBreakingChanges(totalBreaking != null ? totalBreaking : 0L);
        response.setAverageRiskScore(avgRiskScore != null ? avgRiskScore : 0.0);

        List<Comparison> comparisons = comparisonRepository.findByUserOrderByCreatedAtDesc(user);
        response.setChangesBySeverity(calculateChangesBySeverity(comparisons));
        response.setComparisonsByFormat(calculateComparisonsByFormat(comparisons));
        response.setComparisonsByMonth(calculateComparisonsByMonth(comparisons));

        return response;
    }

    private Map<String, Long> calculateChangesBySeverity(List<Comparison> comparisons) {
        Map<String, Long> result = new HashMap<>();
        long breaking = 0, dangerous = 0, warning = 0, info = 0;

        for (Comparison c : comparisons) {
            if (c.getBreakingCount() != null) breaking += c.getBreakingCount();
            if (c.getDangerousCount() != null) dangerous += c.getDangerousCount();
            if (c.getWarningCount() != null) warning += c.getWarningCount();
            if (c.getInfoCount() != null) info += c.getInfoCount();
        }

        result.put("BREAKING", breaking);
        result.put("DANGEROUS", dangerous);
        result.put("WARNING", warning);
        result.put("INFO", info);

        return result;
    }

    private Map<String, Long> calculateComparisonsByFormat(List<Comparison> comparisons) {
        Map<String, Long> result = new HashMap<>();

        for (Comparison c : comparisons) {
            String format = c.getOldSpecFormat() != null ? c.getOldSpecFormat() : "unknown";
            result.merge(format, 1L, Long::sum);
        }

        return result;
    }

    private Map<String, Long> calculateComparisonsByMonth(List<Comparison> comparisons) {
        Map<String, Long> result = new HashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

        for (Comparison c : comparisons) {
            if (c.getCreatedAt() != null) {
                String month = c.getCreatedAt().format(formatter);
                result.merge(month, 1L, Long::sum);
            }
        }

        return result;
    }
}
