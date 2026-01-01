package io.github.mohmk10.changeloghub.api.service;

import io.github.mohmk10.changeloghub.api.dto.UserDto;
import io.github.mohmk10.changeloghub.api.entity.User;
import io.github.mohmk10.changeloghub.api.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByGithubId(Long githubId) {
        return userRepository.findByGithubId(githubId);
    }

    @Transactional
    public User createOrUpdateUser(Long githubId, String username, String email, String avatarUrl) {
        return userRepository.findByGithubId(githubId)
                .map(existingUser -> {
                    existingUser.setUsername(username);
                    existingUser.setEmail(email);
                    existingUser.setAvatarUrl(avatarUrl);
                    existingUser.setLastLogin(LocalDateTime.now());
                    return userRepository.save(existingUser);
                })
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setGithubId(githubId);
                    newUser.setUsername(username);
                    newUser.setEmail(email);
                    newUser.setAvatarUrl(avatarUrl);
                    return userRepository.save(newUser);
                });
    }

    public UserDto toDto(User user) {
        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getAvatarUrl(),
                user.getCreatedAt(),
                user.getLastLogin()
        );
    }
}
