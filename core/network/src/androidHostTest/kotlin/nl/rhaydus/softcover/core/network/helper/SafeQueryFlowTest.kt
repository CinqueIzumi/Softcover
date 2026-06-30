package nl.rhaydus.softcover.core.network.helper

import app.cash.turbine.test
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.ApolloResponse
import com.apollographql.apollo.api.Error
import com.apollographql.cache.normalized.FetchPolicy
import com.apollographql.cache.normalized.fetchPolicy
import com.apollographql.apollo.exception.ApolloException
import com.apollographql.apollo.exception.ApolloHttpException
import com.apollographql.apollo.exception.ApolloNetworkException
import com.apollographql.apollo.exception.CacheMissException
import com.benasher44.uuid.uuid4
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.GetUserIdQuery
import nl.rhaydus.softcover.core.domain.connectivity.NetworkAvailability
import nl.rhaydus.softcover.core.domain.exception.InvalidTokenException
import nl.rhaydus.softcover.core.domain.exception.OfflineException
import nl.rhaydus.softcover.core.domain.exception.ServerUnavailableException
import nl.rhaydus.softcover.core.domain.exception.UnexpectedApiException
import nl.rhaydus.softcover.core.domain.message.SessionExpiredNotifier
import okio.Buffer
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SafeQueryFlowTest {
    private val query = GetUserIdQuery()
    private lateinit var apolloClient: ApolloClient
    private lateinit var apolloCall: ApolloCall<GetUserIdQuery.Data>

    private val cacheData = GetUserIdQuery.Data(
        me = listOf(
            GetUserIdQuery.Data.Me(
                id = 1,
                __typename = "users",
            ),
        ),
    )
    private val networkData = GetUserIdQuery.Data(
        me = listOf(
            GetUserIdQuery.Data.Me(
                id = 2,
                __typename = "users",
            ),
        ),
    )

    @BeforeEach
    fun setUp() {
        apolloClient = mockk()
        @Suppress("UNCHECKED_CAST")
        apolloCall = mockk<ApolloCall<GetUserIdQuery.Data>>()

        mockkObject(SessionExpiredNotifier)
        mockkObject(NetworkAvailability)

        mockkStatic("com.apollographql.cache.normalized.FetchPoliciesKt")

        every { apolloClient.query(any<GetUserIdQuery>()) } returns apolloCall
        every { apolloCall.fetchPolicy(any()) } returns apolloCall
        every { SessionExpiredNotifier.notifySessionExpired() } returns Unit
    }

    private fun dataResponse(data: GetUserIdQuery.Data): ApolloResponse<GetUserIdQuery.Data> =
        ApolloResponse.Builder(
            operation = query,
            requestUuid = uuid4(),
        ).data(data).build()

    private fun errorResponse(errors: List<Error>): ApolloResponse<GetUserIdQuery.Data> =
        ApolloResponse.Builder(
            operation = query,
            requestUuid = uuid4(),
        ).errors(errors).build()

    private fun exceptionResponse(ex: ApolloException): ApolloResponse<GetUserIdQuery.Data> =
        ApolloResponse.Builder(
            operation = query,
            requestUuid = uuid4(),
        ).exception(ex).build()

    private fun httpException(statusCode: Int): ApolloHttpException =
        ApolloHttpException(
            statusCode = statusCode,
            headers = emptyList(),
            body = Buffer(),
            message = "HTTP $statusCode",
        )

    private fun networkException(message: String = "connection failed"): ApolloNetworkException =
        ApolloNetworkException(message = message)

    private fun cacheMissException(): CacheMissException =
        CacheMissException(
            key = "QUERY_ROOT",
            fieldName = "me",
        )

    @Nested
    inner class SuccessfulEmissions {
        @Test
        fun `cache response then network response both emitted in order`() = runTest {
            // ----- Arrange -----
            val cacheResponse = dataResponse(cacheData)
            val networkResponse = dataResponse(networkData)
            every { apolloCall.toFlow() } returns flowOf(
                cacheResponse,
                networkResponse,
            )

            // ----- Act & Assert -----
            apolloClient.safeQueryFlow(query).test {
                awaitItem() shouldBe cacheData
                awaitItem() shouldBe networkData
                awaitComplete()
            }
        }

        @Test
        fun `CacheMissException before network data is silently ignored — only network data emitted`() = runTest {
            // ----- Arrange -----
            val cacheMissResponse = exceptionResponse(cacheMissException())
            val networkResponse = dataResponse(networkData)
            every { apolloCall.toFlow() } returns flowOf(
                cacheMissResponse,
                networkResponse,
            )

            // ----- Act & Assert -----
            apolloClient.safeQueryFlow(query).test {
                awaitItem() shouldBe networkData
                awaitComplete()
            }
        }
    }

    @Nested
    inner class AuthFailures {
        @Test
        fun `cache hit then HTTP 401 — notifies session expired and completes without throwing`() = runTest {
            // ----- Arrange -----
            val cacheResponse = dataResponse(cacheData)
            val authExResponse = exceptionResponse(httpException(401))
            every { apolloCall.toFlow() } returns flowOf(
                cacheResponse,
                authExResponse,
            )

            // ----- Act & Assert -----
            apolloClient.safeQueryFlow(query).test {
                awaitItem() shouldBe cacheData
                awaitComplete()
            }

            verify(exactly = 1) { SessionExpiredNotifier.notifySessionExpired() }
        }

        @Test
        fun `no data emitted then HTTP 403 — throws InvalidTokenException and notifies session expired`() = runTest {
            // ----- Arrange -----
            val authExResponse = exceptionResponse(httpException(403))
            every { apolloCall.toFlow() } returns flowOf(authExResponse)

            // ----- Act & Assert -----
            apolloClient.safeQueryFlow(query).test {
                awaitError().shouldBeInstanceOf<InvalidTokenException>()
            }

            verify(exactly = 1) { SessionExpiredNotifier.notifySessionExpired() }
        }

        @Test
        fun `no data emitted then HTTP 401 — throws InvalidTokenException and notifies session expired`() = runTest {
            // ----- Arrange -----
            val authExResponse = exceptionResponse(httpException(401))
            every { apolloCall.toFlow() } returns flowOf(authExResponse)

            // ----- Act & Assert -----
            apolloClient.safeQueryFlow(query).test {
                awaitError().shouldBeInstanceOf<InvalidTokenException>()
            }

            verify(exactly = 1) { SessionExpiredNotifier.notifySessionExpired() }
        }
    }

    @Nested
    inner class RetryableTransportFailures {
        @Test
        fun `no data emitted, ApolloNetworkException with device offline — throws OfflineException`() = runTest {
            // ----- Arrange -----
            every { NetworkAvailability.isOnline() } returns false
            val networkExResponse = exceptionResponse(networkException())
            every { apolloCall.toFlow() } returns flowOf(networkExResponse)

            // ----- Act & Assert -----
            apolloClient.safeQueryFlow(query).test {
                awaitError().shouldBeInstanceOf<OfflineException>()
            }
        }

        @Test
        fun `no data emitted, ApolloNetworkException with device online — throws ServerUnavailableException`() = runTest {
            // ----- Arrange -----
            every { NetworkAvailability.isOnline() } returns true
            val networkExResponse = exceptionResponse(networkException())
            every { apolloCall.toFlow() } returns flowOf(networkExResponse)

            // ----- Act & Assert -----
            apolloClient.safeQueryFlow(query).test {
                awaitError().shouldBeInstanceOf<ServerUnavailableException>()
            }
        }

        @Test
        fun `no data emitted, transient HTTP 503 — throws ServerUnavailableException`() = runTest {
            // ----- Arrange -----
            val transientExResponse = exceptionResponse(httpException(503))
            every { apolloCall.toFlow() } returns flowOf(transientExResponse)

            // ----- Act & Assert -----
            apolloClient.safeQueryFlow(query).test {
                awaitError().shouldBeInstanceOf<ServerUnavailableException>()
            }
        }

        @Test
        fun `no data emitted, transient HTTP 429 — throws ServerUnavailableException`() = runTest {
            // ----- Arrange -----
            val throttleExResponse = exceptionResponse(httpException(429))
            every { apolloCall.toFlow() } returns flowOf(throttleExResponse)

            // ----- Act & Assert -----
            apolloClient.safeQueryFlow(query).test {
                awaitError().shouldBeInstanceOf<ServerUnavailableException>()
            }
        }
    }

    @Nested
    inner class GenericFailures {
        @Test
        fun `no data emitted, HTTP 400 (non-retryable) — throws UnexpectedApiException`() = runTest {
            // ----- Arrange -----
            val badRequestExResponse = exceptionResponse(httpException(400))
            every { apolloCall.toFlow() } returns flowOf(badRequestExResponse)

            // ----- Act & Assert -----
            apolloClient.safeQueryFlow(query).test {
                awaitError().shouldBeInstanceOf<UnexpectedApiException>()
            }
        }

        @Test
        fun `no data emitted, response with GraphQL errors and null data — throws UnexpectedApiException`() = runTest {
            // ----- Arrange -----
            val graphqlError = Error.Builder(message = "something went wrong").build()
            val graphqlErrorResponse = errorResponse(listOf(graphqlError))
            every { apolloCall.toFlow() } returns flowOf(graphqlErrorResponse)

            // ----- Act & Assert -----
            apolloClient.safeQueryFlow(query).test {
                awaitError().shouldBeInstanceOf<UnexpectedApiException>()
            }
        }

        @Test
        fun `empty flow (no responses at all) — throws UnexpectedApiException`() = runTest {
            // ----- Arrange -----
            every { apolloCall.toFlow() } returns flowOf()

            // ----- Act & Assert -----
            apolloClient.safeQueryFlow(query).test {
                awaitError().shouldBeInstanceOf<UnexpectedApiException>()
            }
        }
    }
}
