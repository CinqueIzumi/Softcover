package nl.rhaydus.softcover.core.domain.model

/**
 * The sentinel stored for "no authenticated user". Persisted, so it is the default a one-shot read of the
 * user-id flow falls back to, and the value [nl.rhaydus.softcover.core.domain.exception.NoUserIdFoundException]
 * is raised for.
 */
const val NO_USER_ID: Int = -1
