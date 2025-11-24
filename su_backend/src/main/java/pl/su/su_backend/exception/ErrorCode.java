package pl.su.su_backend.exception;

public enum ErrorCode {
    // --- GENERIC ERRORS ---
    INTERNAL_SERVER_ERROR,
    UNAUTHORIZED,
    FORBIDDEN,
    BAD_REQUEST,
    RESOURCE_NOT_FOUND,
    CONFLICT,

    // --- DOMAIN SPECIFIC ERRORS ---

    // User related errors
    USER_NOT_FOUND,
    USER_BLOCKED,
    EMAIL_IN_USE,
    INVALID_CREDENTIALS,

    // Permission and role related errors
    ACCESS_DENIED,
    INSUFFICIENT_PERMISSIONS,
    ROLE_NOT_FOUND,
    DEFAULT_ROLE_MISSING,
    CANNOT_MODIFY_HIGHER_RANK,
    CANNOT_REVOKE_LAST_ADMIN,
    INVALID_ROLE_ASSIGNMENT,

    // Class and budget related errors
    BUDGET_NOT_FOUND,
    BUDGET_ALREADY_EXISTS,
    TRANSACTION_NOT_FOUND,

    // General validation errors
    VALIDATION_ERROR
}