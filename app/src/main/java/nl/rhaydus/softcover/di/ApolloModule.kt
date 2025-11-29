package nl.rhaydus.softcover.di

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.network.okHttpClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import nl.rhaydus.softcover.core.data.network.interceptor.AuthInterceptor
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetApiKeyUseCase
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApolloModule {
    @Singleton
    @Provides
    fun getAuthInterceptor(
        getApiKeyUseCase: GetApiKeyUseCase,
    ): AuthInterceptor = AuthInterceptor(getApiKeyUseCase = getApiKeyUseCase)

    @Singleton
    @Provides
    fun getOkHttpClient(
        authInterceptor: AuthInterceptor,
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(interceptor = authInterceptor)
        .build()

    @Singleton
    @Provides
    fun getApolloClient(
        client: OkHttpClient
    ): ApolloClient = ApolloClient.Builder()
        .serverUrl("https://api.hardcover.app/v1/graphql")
        .okHttpClient(client)
        .build()
}