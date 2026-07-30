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
 * <p>This component implements {@link GrantedAuthoritiesMapper} and is
 * invoked by Spring Security after a successful Google OAuth2 login. It
 * extracts the user's email from the OIDC claims, looks up the
 * corresponding {@link User} entity in the database, and maps the user's
 * persisted roles to Spring Security {@link GrantedAuthority} instances.
 * This bridges the gap between the external identity provider's token
 * and the application's internal role-based access control model.</p>
 *
 * <p>For returning users (those with an existing database record), the
 * mapper returns authorities derived from their stored roles, which may
 * include ADMIN, SELLER, or CUSTOMER. For new users (no database record
 * found), the mapper assigns a temporary CUSTOMER authority that allows
 * access to the registration-completion flow. This temporary role ensures
 * that new OAuth2 users can complete their profile without being blocked
 * by authorization checks.</p>
 *
 * @author PrabhatKrMishra
 * @see User
 * @see UserRepository
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

    /**
     * Maps the OIDC authorities extracted from a Google OAuth2 token into
     * application-specific granted authorities.
     *
     * <p>This method iterates over each authority returned by the OIDC
     * provider. When an {@link OidcUserAuthority} is found, it extracts
     * the user's email from the attributes map and performs a database
     * lookup. If the user exists, their persisted roles are converted to
     * {@link SimpleGrantedAuthority} instances and returned. If the user
     * does not exist, a single CUSTOMER authority is assigned to allow
     * the user to complete the registration flow.</p>
     *
     * <p>Roles with null or blank names are filtered out and logged as
     * warnings to prevent misconfigured database entries from causing
     * authentication failures. The returned set is de-duplicated to avoid
     * multiple instances of the same authority.</p>
     *
     * @param authorities the authorities extracted from the OIDC token
     * @return a set of mapped {@link GrantedAuthority} instances
     */
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
