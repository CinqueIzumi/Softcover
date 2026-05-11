package nl.rhaydus.softcover.core.data.network.helper

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.ApolloResponse
import com.apollographql.apollo.api.Mutation
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.api.Query
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.apollographql.apollo.exception.ApolloNetworkException
import com.apollographql.apollo.exception.CacheMissException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import nl.rhaydus.softcover.core.domain.connectivity.NetworkAvailability
import nl.rhaydus.softcover.core.domain.exception.OfflineException

private fun <T : Operation.Data> requireData(response: ApolloResponse<T>): T {
    response.exception?.let { exception ->
        if (exception is ApolloNetworkException && NetworkAvailability.isOnline().not()) {
            throw OfflineException()
        }

        throw RuntimeException(
            "Apollo network error: ${exception.message}",
            exception,
        )
    }

    if (response.errors.isNullOrEmpty().not()) {
        val message = response.errors?.joinToString { error ->
            buildString {
                append(error.message)

                error.path?.let { path ->
                    append(" | path=$path")
                }

                error.extensions?.let { append(" | extensions=$it") }
            }
        } ?: ""

        throw RuntimeException("Apollo GraphQL error(s): \n$message")
    }

    return response.data ?: throw RuntimeException("Apollo response had no data and no errors")
}

private suspend fun <T : Operation.Data> executeCall(
    requireNetwork: Boolean,
    call: suspend () -> ApolloResponse<T>,
): T {
    if (requireNetwork && NetworkAvailability.isOnline().not()) {
        throw OfflineException()
    }

    return requireData(call())
}

suspend fun <T : Mutation.Data> ApolloClient.safeMutation(mutation: Mutation<T>): T =
    executeCall(requireNetwork = true) { this.mutation(mutation).execute() }

suspend fun <T : Query.Data> ApolloClient.safeQuery(
    query: Query<T>,
    fetchPolicy: FetchPolicy = FetchPolicy.NetworkOnly,
): T = executeCall(requireNetwork = fetchPolicy == FetchPolicy.NetworkOnly) {
    this.query(query).fetchPolicy(fetchPolicy).execute()
}

fun <T : Query.Data> ApolloClient.safeQueryFlow(
    query: Query<T>,
    fetchPolicy: FetchPolicy = FetchPolicy.CacheAndNetwork,
): Flow<T> = flow {
    var lastFailure: Throwable? = null
    var emittedAny = false

    this@safeQueryFlow.query(query).fetchPolicy(fetchPolicy).toFlow().collect { response ->
        val data = response.data

        if (data != null && response.errors.isNullOrEmpty()) {
            emit(data)
            emittedAny = true
        } else {
            response.exception?.let { exception ->
                if (exception !is CacheMissException) {
                    lastFailure = exception
                }
            }
        }
    }

    if (emittedAny.not()) {
        val failure = lastFailure

        if (failure is ApolloNetworkException && NetworkAvailability.isOnline().not()) {
            throw OfflineException()
        }

        throw failure ?: RuntimeException("Apollo flow completed with no data")
    }
}
