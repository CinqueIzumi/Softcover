package nl.rhaydus.softcover.core.data.network.helper

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.ApolloResponse
import com.apollographql.apollo.api.Mutation
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.api.Query

private suspend fun <T : Operation.Data> executeCall(
    call: suspend () -> ApolloResponse<T>,
): T {
    val response = call()

    response.exception?.let { exception ->
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

suspend fun <T : Mutation.Data> ApolloClient.safeMutation(mutation: Mutation<T>): T =
    executeCall { this.mutation(mutation).execute() }

suspend fun <T : Query.Data> ApolloClient.safeQuery(query: Query<T>): T =
    executeCall { this.query(query).execute() }