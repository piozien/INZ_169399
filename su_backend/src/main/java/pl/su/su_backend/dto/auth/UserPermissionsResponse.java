package pl.su.su_backend.dto.auth;

import java.util.Set;

public record UserPermissionsResponse(
        Set<String> roles,
        Set<String> permissions
) {}