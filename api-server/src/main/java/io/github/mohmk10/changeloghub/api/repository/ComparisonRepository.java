package io.github.mohmk10.changeloghub.api.repository;

import io.github.mohmk10.changeloghub.api.entity.Comparison;
import io.github.mohmk10.changeloghub.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface ComparisonRepository extends JpaRepository<Comparison, UUID> {
    List<Comparison> findByUserOrderByCreatedAtDesc(User user);

    @Query("SELECT COUNT(c) FROM Comparison c WHERE c.user = :user")
    Long countByUser(User user);

    @Query("SELECT SUM(c.breakingCount) FROM Comparison c WHERE c.user = :user")
    Long sumBreakingCountByUser(User user);

    @Query("SELECT AVG(c.riskScore) FROM Comparison c WHERE c.user = :user")
    Double avgRiskScoreByUser(User user);
}
