package nl.rhaydus.softcover.di

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.api.MemoryCacheFactory
import com.apollographql.apollo.cache.normalized.normalizedCache
import com.apollographql.apollo.network.okHttpClient
import nl.rhaydus.softcover.core.data.network.interceptor.AuthInterceptor
import okhttp3.OkHttpClient
import org.koin.dsl.module

val apolloModule = module {
    single {
        AuthInterceptor()
    }

    single {
        OkHttpClient.Builder()
            .addInterceptor(get<AuthInterceptor>())
            .build()
    }

    single {
        ApolloClient.Builder()
            .serverUrl("https://api.hardcover.app/v1/graphql")
            .okHttpClient(get())
            .normalizedCache(MemoryCacheFactory())
            .build()
    }
}