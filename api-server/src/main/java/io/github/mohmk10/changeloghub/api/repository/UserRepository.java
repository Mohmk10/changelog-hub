package io.github.mohmk10.changeloghub.api.repository;

import io.github.mohmk10.changeloghub.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByGithubId(Long githubId);
    Optional<User> findByUsername(String username);
}
