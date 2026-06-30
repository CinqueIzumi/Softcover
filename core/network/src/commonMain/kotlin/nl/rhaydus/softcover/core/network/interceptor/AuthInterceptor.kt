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

internal class AuthInterceptor(
    private val authTokenProvider: AuthTokenProvider,
) : ApolloInterceptor {
    override fun <D : Operation.Data> intercept(
        request: ApolloRequest<D>,
        chain: ApolloInterceptorChain,
    ): Flow<ApolloResponse<D>> = flow {
        // Read the token fresh on every request: it is backed by an in-memory StateFlow, so this is
        // cheap, and it ensures a token change (re-auth) or clear (logout) takes effect immediately
        // rather than persisting a stale token until the next process restart.
        val token = authTokenProvider.apiKey.firstOrNull()?.ifBlank { null }

        val updatedRequest = if (token != null) {
            request
                .newBuilder()
                .addHttpHeader(
                    "Authorization",
                    "Bearer $token",
                )
                .build()
        } else {
            request
        }

        emitAll(chain.proceed(updatedRequest))
    }
}
