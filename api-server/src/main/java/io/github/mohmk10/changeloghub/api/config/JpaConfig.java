package io.github.mohmk10.changeloghub.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "io.github.mohmk10.changeloghub.api.repository")
public class JpaConfig {
}
