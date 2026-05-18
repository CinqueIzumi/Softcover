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
import kotlinx.coroutines.flow.toList
import nl.rhaydus.softcover.core.domain.connectivity.NetworkAvailability
import nl.rhaydus.softcover.core.domain.exception.OfflineException
import nl.rhaydus.softcover.core.presentation.util.SnackBarManager

private const val GENERIC_ERROR_MESSAGE = "Something went wrong"

private fun notifyGenericError() {
    SnackBarManager.showSnackbar(title = GENERIC_ERROR_MESSAGE)
}

private fun <T : Operation.Data> requireData(response: ApolloResponse<T>): T {
    response.exception?.let { exception ->
        if (exception is ApolloNetworkException && NetworkAvailability.isOnline().not()) {
            throw OfflineException()
        }

        notifyGenericError()

        throw RuntimeException(
            "Apollo error: ${exception.message}",
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

        notifyGenericError()

        throw RuntimeException("Apollo GraphQL error(s): \n$message")
    }

    return response.data ?: run {
        notifyGenericError()

        throw RuntimeException("Apollo response had no data and no errors")
    }
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
    // Apollo Kotlin v4's CacheFirst/NetworkFirst interceptors emit TWO responses
    // on the failure path (the failed-stage response, then the fallback). Calling
    // .execute() surfaces the first emission, so a cache miss reaches us as a
    // CacheMissException even though the network leg would have succeeded.
    // Consume the flow and pick the last data-bearing response; fall back to the
    // last emission so genuine errors (offline, GraphQL errors) still propagate.
    val responses = this.query(query).fetchPolicy(fetchPolicy).toFlow().toList()

    responses.lastOrNull { it.data != null && it.errors.isNullOrEmpty() }
        ?: responses.last()
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

        notifyGenericError()

        throw failure ?: RuntimeException("Apollo flow completed with no data")
    }
}
