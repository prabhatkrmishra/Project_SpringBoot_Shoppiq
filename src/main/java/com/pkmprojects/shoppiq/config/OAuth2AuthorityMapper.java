package com.pkmprojects.shoppiq.config;

import com.pkmprojects.shoppiq.entity.user.User;
import com.pkmprojects.shoppiq.repository.user.UserRepository;
import com.pkmprojects.shoppiq.service.role.RoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Maps OIDC authorities from Google OAuth2 into application-specific roles.
 *
 * <p>Returning users receive authorities derived from their database roles.
 * New users receive a temporary CUSTOMER authority that allows access to
 * the registration-completion flow until a local account is created.</p>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Component
public class OAuth2AuthorityMapper implements GrantedAuthoritiesMapper {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2AuthorityMapper.class);

    private final UserRepository userRepository;
    private final RoleService rolesService;

    public OAuth2AuthorityMapper(UserRepository userRepository, RoleService rolesService) {
        this.userRepository = userRepository;
        this.rolesService = rolesService;
    }

    @Override
    public Set<GrantedAuthority> mapAuthorities(Collection<? extends GrantedAuthority> authorities) {
        Set<GrantedAuthority> mappedAuthorities = new HashSet<>();

        authorities.forEach(authority -> {
            if (authority instanceof OidcUserAuthority oidcUserAuthority) {
                Map<String, Object> userAttributesMap = oidcUserAuthority.getAttributes();
                String email = (String) userAttributesMap.get("email");

                if (email != null) {
                    User user = userRepository.findUserByEmail(email).orElse(null);

                    if (user != null) {
                        Set<GrantedAuthority> userAuthorities = user.getRoles().stream()
                                .filter(role -> {
                                    boolean hasValidName = role.getRoleName() != null && !role.getRoleName().isBlank();
                                    if (!hasValidName) {
                                        logger.warn("User '{}' has role id={} with a null/blank roleName",
                                                email, role.getId());
                                    }
                                    return hasValidName;
                                })
                                .map(role -> new SimpleGrantedAuthority(role.getRoleName()))
                                .collect(Collectors.toSet());
                        mappedAuthorities.addAll(userAuthorities);
                        logger.debug("Mapped returning OAuth2 user '{}' to roles: {}", email, user.getRoles());
                    } else {
                        mappedAuthorities.add(new SimpleGrantedAuthority(rolesService.getCustomerRole().getRoleName()));
                        logger.debug("Mapped new OAuth2 user '{}' to temporary role: CUSTOMER", email);
                    }
                }
            }
        });

        return mappedAuthorities;
    }
}
