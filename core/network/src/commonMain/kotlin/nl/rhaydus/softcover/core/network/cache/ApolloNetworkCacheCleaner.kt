package nl.rhaydus.softcover.core.network.cache

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.apolloStore
import nl.rhaydus.softcover.core.domain.cache.NetworkCacheCleaner

internal class ApolloNetworkCacheCleaner(
    private val apolloClient: ApolloClient,
) : NetworkCacheCleaner {
    override suspend fun clearAll() {
        apolloClient.apolloStore.clearAll()
    }
}
