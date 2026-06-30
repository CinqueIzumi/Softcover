package nl.rhaydus.softcover.core.domain.exception

/**
 * The device reports connectivity, but the request never got a processable response from the server:
 * a refused/timed-out connection, DNS/socket failure, or a 5xx/throttle status. Transient and
 * retryable — see [RetryableSyncException].
 */
class ServerUnavailableException(
    message: String,
    cause: Throwable? = null,
) : RetryableSyncException(message, cause)
