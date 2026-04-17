package nl.rhaydus.softcover.di

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.network.okHttpClient
import nl.rhaydus.softcover.core.data.network.interceptor.AuthInterceptor
import okhttp3.OkHttpClient
import org.koin.dsl.module
import java.util.concurrent.TimeUnit

val apolloModule = module {
    single {
        AuthInterceptor(apiKeyLocalDataSource = get())
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
            .build()
    }
}