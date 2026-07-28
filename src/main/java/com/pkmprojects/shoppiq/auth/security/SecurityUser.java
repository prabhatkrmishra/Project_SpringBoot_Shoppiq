package com.pkmprojects.shoppiq.auth.security;

import com.pkmprojects.shoppiq.entity.user.User;
import com.pkmprojects.shoppiq.entity.role.Role;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * <strong>Spring Boot Concept:</strong> Spring Security adapter that wraps a {@link User} entity as a
 * {@link UserDetails} object.
 *
 * <h3>Spring Security concepts demonstrated</h3>
 * <ul>
 *   <li><strong>UserDetails interface</strong> — the core contract that Spring
 *       Security uses to represent a principal. Every authenticated user is
 *       represented as a {@code UserDetails} somewhere in the
 *       {@link org.springframework.security.core.Authentication} object.</li>
 *   <li><strong>Adapter / Wrapper pattern</strong> — decouples the JPA entity
 *       ({@link User}) from the security framework. The entity remains a pure
 *       domain object while this adapter translates it for the security
 *       infrastructure, following the Single Responsibility Principle.</li>
 *   <li><strong>GrantedAuthority mapping</strong> — translates the entity's
 *       {@link com.pkmprojects.shoppiq.entity.role.Role} set into Spring
 *       Security {@link org.springframework.security.core.GrantedAuthority}
 *       objects (e.g., {@code "ROLE_CUSTOMER"}, {@code "ROLE_ADMIN"}).</li>
 *   <li><strong>Account status delegation</strong> — {@link #isAccountNonLocked()}
 *       and {@link #isEnabled()} delegate to the entity, enabling database-driven
 *       account locking and disabling without modifying the security adapter.</li>
 * </ul>
 *
 * <h3>Authentication flow</h3>
 * <ul>
 *   <li><b>Password login:</b> {@link com.pkmprojects.shoppiq.auth.service.CustomUserDetailService}
 *       creates a {@code SecurityUser} via {@code loadUserByUsername()}.
 *       {@code DaoAuthenticationProvider} uses it to verify the password.</li>
 *   <li><b>JWT authentication:</b> {@link com.pkmprojects.shoppiq.auth.jwt.JwtAuthenticationFilter}
 *       creates a {@code SecurityUser} directly from the database entity and
 *       wraps it in a {@link org.springframework.security.authentication.UsernamePasswordAuthenticationToken}.</li>
 *   <li><b>Controller injection:</b> controllers access the domain {@link User}
 *       via SpEL: {@code @AuthenticationPrincipal(expression = "user") User user}.</li>
 * </ul>
 *
 * <h3>Design patterns</h3>
 * <ul>
 *   <li><strong>Adapter pattern</strong> — adapts {@link User} → {@link UserDetails}
 *       without modifying either interface.</li>
 *   <li><strong>Delegation</strong> — account-status methods delegate to the
 *       entity, keeping the security logic in the domain layer.</li>
 *   <li><strong>Immutable wrapper</strong> — the wrapped {@link User} is
 *       accessible via {@code @Getter} but the adapter does not modify it.</li>
 * </ul>
 *
 * @author prabhatkrmishra
 * @see com.pkmprojects.shoppiq.auth.service.CustomUserDetailService
 * @since 1.4.0
 */
@RequiredArgsConstructor
public class SecurityUser implements UserDetails {

    /**
     * The wrapped domain user.
     */
    @Getter
    private final User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getRoleName()))
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return user.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.isEnabled();
    }
}
