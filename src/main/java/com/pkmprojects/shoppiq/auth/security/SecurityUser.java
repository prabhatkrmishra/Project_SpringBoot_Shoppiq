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
 * Spring Security adapter that wraps a {@link User} entity.
 *
 * <p>Decouples the domain {@link User} from Spring Security's
 * {@link UserDetails} interface. The entity remains a pure JPA
 * aggregate while this adapter translates it for the security
 * infrastructure.</p>
 *
 * <h2>Design</h2>
 * <ul>
 *     <li>Immutable wrapper — does not modify the wrapped {@link User}.</li>
 *     <li>Delegates account-status queries ({@code isAccountNonLocked},
 *         {@code isEnabled}, etc.) to the entity.</li>
 *     <li>Builds {@link GrantedAuthority} instances from the user's
 *         {@link Role} set.</li>
 * </ul>
 *
 * <h2>Usage in Controllers</h2>
 * <p>Controllers inject the domain {@link User} directly via SpEL:</p>
 * <pre>
 * &#64;GetMapping("/profile")
 * public ResponseEntity&lt;UserResponse&gt; getProfile(
 *         &#64;AuthenticationPrincipal(expression = "user") User user) { ... }
 * </pre>
 *
 * @author PrabhatKrMishra
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
