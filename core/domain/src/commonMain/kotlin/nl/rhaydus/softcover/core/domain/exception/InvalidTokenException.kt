package nl.rhaydus.softcover.core.domain.exception

/**
 * Thrown when the server rejects the request because the auth token is missing, expired, or revoked
 * (HTTP 401/403). Distinct from a generic failure so callers can drive a re-auth prompt rather than a
 * generic error toast. Not retryable — replaying with the same token would fail identically.
 */
class InvalidTokenException(
    message: String = "Authentication token is invalid",
) : Exception(message)
