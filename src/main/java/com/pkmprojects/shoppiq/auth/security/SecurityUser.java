package com.pkmprojects.shoppiq.auth.security;

import com.pkmprojects.shoppiq.auth.service.CustomUserDetailService;
import com.pkmprojects.shoppiq.entity.user.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Adapter that wraps a {@link User} entity as a Spring Security {@link UserDetails} object.
 *
 * <p>This class bridges the gap between the application's domain model and
 * Spring Security's authentication contract. It wraps the {@link User}
 * entity and delegates account status checks (enabled, locked, expired)
 * to the underlying entity's fields. The {@link #getAuthorities()} method
 * converts the user's roles into {@link SimpleGrantedAuthority} instances
 * that Spring Security uses for authorization decisions.</p>
 *
 * <p>The adapter pattern keeps the domain model clean of Spring Security
 * dependencies while allowing seamless integration with the security
 * framework. The wrapped {@link User} entity is accessible through the
 * {@link #user ()} method for cases where the full domain object is
 * needed (e.g., audit logging, profile updates).</p>
 *
 * @param user The wrapped domain user.
 * @author prabhatkrmishra
 * @see CustomUserDetailService
 * @since 1.4.0
 */
public record SecurityUser(User user) implements UserDetails {

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
