package nl.rhaydus.softcover.core.domain.exception

/**
 * Marks a failure that is *transient* — the request never reached a server that could process it
 * (no connectivity, refused/timed-out connection, or a 5xx/throttle response). Writes that fail with
 * one of these are safe to enqueue and replay later; reads can be retried.
 *
 * A successful call whose payload the server *rejected* (GraphQL errors, an empty body, or a 4xx
 * response) is NOT one of these — replaying it would fail identically, so it must not be retried.
 */
sealed class RetryableSyncException(
    message: String,
    cause: Throwable? = null,
) : ApiException(message, cause)
