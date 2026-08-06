package gasi.one.platform.infrastructure.config;

import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Enables JPA auditing and provides the current auditor (username) for auditing
 * purposes.
 * <p>
 * The auditor is obtained from Spring Security's current
 * {@link Authentication}. If no authenticated user is found, it defaults to
 * {@code system}. This allows JPA auditing annotations like {@code @CreatedBy}
 * and {@code @LastModifiedBy} to automatically populate with the current user's
 * identity when an authentication plugin is active.
 * <p>
 * Authentication plugins should populate the standard Spring Security context
 * before persistence operations that require user auditing.
 *
 * @since 1.0.0
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {

    /**
     * Creates the JPA auditing configuration.
     */
    public JpaAuditingConfig() {
    }

    /**
     * Provides the current auditor for Spring Data JPA auditing fields.
     *
     * @return auditor provider that resolves the username or falls back to
     *         {@code system}
     */
    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getName)
                .filter(name -> name != null && !name.isBlank())
                .or(() -> Optional.of("system"));
    }
}
