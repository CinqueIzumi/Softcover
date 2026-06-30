package nl.rhaydus.softcover.core.domain.exception

/**
 * An Apollo failure that is neither retryable nor an auth rejection: a GraphQL error, an empty body, or
 * a 4xx the server understood and refused. Replaying would fail identically, so it surfaces as a genuine
 * error for presentation to report (the typed replacement for the seam's former raw [RuntimeException]).
 */
class UnexpectedApiException(
    message: String,
    cause: Throwable? = null,
) : ApiException(message, cause)
