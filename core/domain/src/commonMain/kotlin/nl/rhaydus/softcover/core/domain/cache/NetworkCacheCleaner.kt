package nl.rhaydus.softcover.core.domain.cache

/**
 * Clears the network layer's normalized response cache. Lives in domain so the account-reset flow can
 * evict cached GraphQL responses on logout / account-switch without depending on the network module.
 */
interface NetworkCacheCleaner {
    suspend fun clearAll()
}
