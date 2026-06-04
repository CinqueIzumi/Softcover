package nl.rhaydus.softcover.core.network.interceptor

import com.apollographql.apollo.api.ApolloRequest
import com.apollographql.apollo.api.ApolloResponse
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.interceptor.ApolloInterceptor
import com.apollographql.apollo.interceptor.ApolloInterceptorChain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import nl.rhaydus.softcover.core.domain.auth.AuthTokenProvider
import kotlin.concurrent.Volatile

internal class AuthInterceptor(
    private val authTokenProvider: AuthTokenProvider,
) : ApolloInterceptor {
    @Volatile
    private var cachedToken: String? = null

    override fun <D : Operation.Data> intercept(
        request: ApolloRequest<D>,
        chain: ApolloInterceptorChain,
    ): Flow<ApolloResponse<D>> = flow {
        if (cachedToken == null) {
            cachedToken = authTokenProvider.apiKey.firstOrNull()?.ifBlank { null }
        }

        val updatedRequest = if (cachedToken != null) {
            request
                .newBuilder()
                .addHttpHeader(
                    "Authorization",
                    "Bearer $cachedToken",
                )
                .build()
        } else {
            request
        }

        emitAll(chain.proceed(updatedRequest))
    }
}
