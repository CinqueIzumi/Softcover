package nl.rhaydus.softcover.core.data.network.di

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.api.MemoryCacheFactory
import com.apollographql.apollo.cache.normalized.api.TypePolicyCacheKeyGenerator
import com.apollographql.apollo.cache.normalized.normalizedCache
import com.apollographql.apollo.network.okHttpClient
import nl.rhaydus.softcover.core.data.network.cache.SoftcoverCacheResolver
import nl.rhaydus.softcover.core.data.network.interceptor.AuthInterceptor
import okhttp3.OkHttpClient
import org.koin.dsl.module
import java.util.concurrent.TimeUnit

private const val APOLLO_MEMORY_CACHE_BYTES = 10 * 1024 * 1024

val apolloModule = module {
    single {
        AuthInterceptor(authTokenProvider = get())
    }

    single {
        OkHttpClient.Builder()
            .addInterceptor(get<AuthInterceptor>())
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    single {
        ApolloClient.Builder()
            .serverUrl("https://api.hardcover.app/v1/graphql")
            .okHttpClient(get())
            .normalizedCache(
                normalizedCacheFactory = MemoryCacheFactory(maxSizeBytes = APOLLO_MEMORY_CACHE_BYTES),
                cacheKeyGenerator = TypePolicyCacheKeyGenerator,
                cacheResolver = SoftcoverCacheResolver,
            )
            .build()
    }
}