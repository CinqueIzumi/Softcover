package nl.rhaydus.softcover.core.network.di

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.api.MemoryCacheFactory
import com.apollographql.apollo.cache.normalized.api.TypePolicyCacheKeyGenerator
import com.apollographql.apollo.cache.normalized.normalizedCache
import com.apollographql.apollo.network.http.DefaultHttpEngine
import org.koin.dsl.module
import nl.rhaydus.softcover.core.domain.cache.NetworkCacheCleaner
import nl.rhaydus.softcover.core.domain.di.dispatcherModule
import nl.rhaydus.softcover.core.network.cache.ApolloNetworkCacheCleaner
import nl.rhaydus.softcover.core.network.cache.SoftcoverCacheResolver
import nl.rhaydus.softcover.core.network.interceptor.AuthInterceptor

private const val APOLLO_MEMORY_CACHE_BYTES = 10 * 1024 * 1024
private const val NETWORK_TIMEOUT_MILLIS = 60_000L

val apolloModule = module {
    includes(dispatcherModule)

    single {
        AuthInterceptor(authTokenProvider = get())
    }

    single {
        ApolloClient.Builder()
            .serverUrl("https://api.hardcover.app/v1/graphql")
            .httpEngine(DefaultHttpEngine(timeoutMillis = NETWORK_TIMEOUT_MILLIS))
            .addInterceptor(get<AuthInterceptor>())
            .normalizedCache(
                normalizedCacheFactory = MemoryCacheFactory(maxSizeBytes = APOLLO_MEMORY_CACHE_BYTES),
                cacheKeyGenerator = TypePolicyCacheKeyGenerator,
                cacheResolver = SoftcoverCacheResolver,
            )
            .build()
    }

    single<NetworkCacheCleaner> {
        ApolloNetworkCacheCleaner(apolloClient = get())
    }
}
