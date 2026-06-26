package nl.rhaydus.softcover.core.domain.exception

/**
 * Root of the typed failures the Apollo network seam raises. Sealed so callers can fold over the
 * failure *kind* exhaustively (offline vs server-down vs auth-rejected vs unexpected) rather than
 * collapsing every failure to an opaque [RuntimeException].
 */
sealed class ApiException(
    message: String?,
    cause: Throwable?,
) : Exception(message, cause)
