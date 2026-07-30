package com.pkmprojects.shoppiq.dto.admin.request;

import com.pkmprojects.shoppiq.dto.user.UserRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Request DTO for bulk creation of user accounts by an administrator.
 *
 * <p>This record wraps a list of {@link com.pkmprojects.shoppiq.dto.user.UserRequest}
 * entries and is submitted to the admin bulk user endpoint for creating
 * multiple user accounts in a single API call. It is primarily used
 * for test-data population during development and staging, enabling
 * administrators to provision realistic user bases at scale.</p>
 *
 * <p>Each element in the list undergoes cascading validation via
 * {@link jakarta.validation.Valid @Valid}, ensuring that user
 * registration fields (name, email, username, password) meet their
 * respective constraints. Passwords are encrypted at the service
 * layer before persistence. The list must not be empty.</p>
 *
 * @param users list of user creation requests, each containing all
 *              required registration fields; must not be empty; each
 *              element is validated recursively via
 *              {@link com.pkmprojects.shoppiq.dto.user.UserRequest}
 * @author prabhatkrmishra
 * @since 1.0.0
 */
public record BulkUserRequest(
        /**
         * List of user creation requests. Must not be empty.
         */
        @NotEmpty(message = "At least one user is required.")
        List<@Valid UserRequest> users
) {
}
