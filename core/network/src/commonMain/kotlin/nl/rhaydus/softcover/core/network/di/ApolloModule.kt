package nl.rhaydus.softcover.core.network.di

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.network.http.DefaultHttpEngine
import com.apollographql.cache.normalized.api.TypePolicyCacheKeyGenerator
import com.apollographql.cache.normalized.memory.MemoryCacheFactory
import com.apollographql.apollo.interceptor.ApolloInterceptor
import com.apollographql.cache.normalized.normalizedCache
import kotlin.time.TimeSource
import org.koin.dsl.module
import nl.rhaydus.softcover.cache.Cache
import nl.rhaydus.softcover.core.domain.cache.NetworkCacheCleaner
import nl.rhaydus.softcover.core.domain.di.dispatcherModule
import nl.rhaydus.softcover.core.network.cache.ApolloNetworkCacheCleaner
import nl.rhaydus.softcover.core.network.cache.SoftcoverCacheResolver
import nl.rhaydus.softcover.core.network.interceptor.ApiRateLimitTier
import nl.rhaydus.softcover.core.network.interceptor.AuthInterceptor
import nl.rhaydus.softcover.core.network.interceptor.RateLimitInterceptor

private const val APOLLO_MEMORY_CACHE_BYTES = 10 * 1024 * 1024
private const val NETWORK_TIMEOUT_MILLIS = 60_000L

val apolloModule = module {
    includes(dispatcherModule)

    single {
        AuthInterceptor(authTokenProvider = get())
    }

    // Bound rather than left to the constructor default so the graph is honest about what the
    // interceptor depends on: Koin's `verify()` reflects over the primary constructor and cannot see
    // that a parameter has a default, so an unbound TimeSource fails module verification. Mirrors
    // how `Clock` / `TimeZone` are bound in profileModule.
    single<TimeSource> { TimeSource.Monotonic }

    // The one place the app decides which rate-limit budget it is entitled to. Fixed to LEGACY_JWT
    // because that is what every install is actually on: the token migration has not run yet, so every
    // signed-in user still holds an outdated JWT and the API caps them at a burst of 5. Binding FREE's
    // 10 here would over-promise by 2x and hand back exactly the 429s this interceptor exists to
    // prevent.
    //
    // Once the migration can tell a migrated token from an outdated one, resolve the tier here — and
    // reading the account's plan (§11's TODO) lands in the same place. Both are a change to *this*
    // binding; the interceptor needs no edit. Never widen the fallback: when the tier cannot be
    // established, the narrowest applicable bucket is the only safe answer, because a bucket wider
    // than the server's is refused rather than delayed.
    single { ApiRateLimitTier.LEGACY_JWT }

    single {
        val tier = get<ApiRateLimitTier>()

        RateLimitInterceptor(
            fallbackBucketSize = tier.bucketSize,
            refillTokensPerSecond = tier.refillTokensPerSecond,
            timeSource = get(),
        )
    }

    single {
        ApolloClient.Builder()
            .serverUrl("https://api.hardcover.app/v1/graphql")
            .httpEngine(DefaultHttpEngine(timeoutMillis = NETWORK_TIMEOUT_MILLIS))
            .addInterceptor(get<AuthInterceptor>())
            // BeforeNetwork, not the default BeforeCache. Apollo's built-in order is
            // cache -> APQ -> retryOnError -> network, so a BeforeCache interceptor also runs for a
            // request the cache answers on its own. The bucket exists to model the *server's* budget,
            // and the server is not consulted by a CacheFirst hit — charging a token for one would
            // throttle the app against budget it never spent. Sitting immediately before the network
            // interceptor means a token is spent exactly when a request actually goes out, including
            // for each attempt Apollo's own retryOnError makes.
            .addInterceptor(
                get<RateLimitInterceptor>(),
                ApolloInterceptor.InsertionPoint.BeforeNetwork,
            )
            .normalizedCache(
                normalizedCacheFactory = MemoryCacheFactory(maxSizeBytes = APOLLO_MEMORY_CACHE_BYTES),
                cacheKeyGenerator = TypePolicyCacheKeyGenerator(typePolicies = Cache.typePolicies),
                cacheResolver = SoftcoverCacheResolver,
            )
            .build()
    }

    single<NetworkCacheCleaner> {
        ApolloNetworkCacheCleaner(apolloClient = get())
    }
}
