package nl.rhaydus.softcover.core.network.helper

import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.ApolloResponse
import com.apollographql.apollo.api.http.HttpHeader
import com.apollographql.apollo.exception.ApolloException
import com.apollographql.apollo.exception.ApolloHttpException
import com.apollographql.apollo.exception.ApolloNetworkException
import com.apollographql.cache.normalized.FetchPolicy
import com.apollographql.cache.normalized.fetchPolicy
import com.benasher44.uuid.uuid4
import app.cash.turbine.test
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import okio.Buffer
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.platform.NetworkAvailability
import nl.rhaydus.softcover.GetUserIdQuery
import nl.rhaydus.softcover.RemoveUserBookMutation
import nl.rhaydus.softcover.core.domain.exception.InvalidTokenException
import nl.rhaydus.softcover.core.domain.exception.OfflineException
import nl.rhaydus.softcover.core.domain.exception.ServerUnavailableException
import nl.rhaydus.softcover.core.domain.exception.UnexpectedApiException
import nl.rhaydus.softcover.core.domain.message.SessionExpiredNotifier

class RetryTransientFailuresTest {
    private val query = GetUserIdQuery()
    private val mutation = RemoveUserBookMutation(id = 1)
    private lateinit var apolloClient: ApolloClient
    private lateinit var apolloCall: ApolloCall<GetUserIdQuery.Data>
    private lateinit var apolloMutationCall: ApolloCall<RemoveUserBookMutation.Data>

    private val successData = GetUserIdQuery.Data(
        me = listOf(
            GetUserIdQuery.Data.Me(
                id = 1,
                __typename = "users",
            ),
        ),
    )

    @BeforeEach
    fun setUp() {
        apolloClient = mockk()
        apolloCall = mockk()
        apolloMutationCall = mockk()

        mockkObject(NetworkAvailability)
        mockkObject(SessionExpiredNotifier)
        mockkStatic("com.apollographql.cache.normalized.FetchPoliciesKt")

        every {
            apolloClient.query(any<GetUserIdQuery>())
        } returns apolloCall
        every {
            apolloCall.fetchPolicy(any())
        } returns apolloCall
        every {
            apolloClient.mutation(any<RemoveUserBookMutation>())
        } returns apolloMutationCall
        every {
            NetworkAvailability.isOnline()
        } returns true
        every {
            SessionExpiredNotifier.notifySessionExpired()
        } returns Unit
    }

    private fun dataResponse(): ApolloResponse<GetUserIdQuery.Data> =
        ApolloResponse.Builder(
            operation = query,
            requestUuid = uuid4(),
        ).data(successData).build()

    private fun exceptionResponse(exception: ApolloException): ApolloResponse<GetUserIdQuery.Data> =
        ApolloResponse.Builder(
            operation = query,
            requestUuid = uuid4(),
        ).exception(exception).build()

    private fun mutationExceptionResponse(exception: ApolloException): ApolloResponse<RemoveUserBookMutation.Data> =
        ApolloResponse.Builder(
            operation = mutation,
            requestUuid = uuid4(),
        ).exception(exception).build()

    private fun httpException(
        statusCode: Int,
        headers: List<HttpHeader> = emptyList(),
    ): ApolloHttpException = ApolloHttpException(
        statusCode = statusCode,
        headers = headers,
        body = Buffer(),
        message = "HTTP $statusCode",
    )

    @Nested
    inner class SafeQueryRetries {
        @Test
        fun `429 is retried and a subsequent success is returned`() = runTest {
            // ----- Arrange -----
            every {
                apolloCall.toFlow()
            } returnsMany listOf(
                flowOf(exceptionResponse(httpException(429))),
                flowOf(dataResponse()),
            )

            // ----- Act -----
            val result = apolloClient.safeQuery(query)

            // ----- Assert -----
            result shouldBe successData
        }

        @Test
        fun `retries stop after the bound and ServerUnavailableException surfaces`() = runTest {
            // ----- Arrange -----
            every {
                apolloCall.toFlow()
            } returns flowOf(exceptionResponse(httpException(429)))

            // ----- Act & Assert -----
            shouldThrow<ServerUnavailableException> {
                apolloClient.safeQuery(query)
            }

            // one initial attempt plus MAX_TRANSIENT_RETRIES (3) retries
            verify(exactly = 4) { apolloCall.toFlow() }
        }

        @Test
        fun `Retry-After header overrides the computed exponential backoff`() = runTest {
            // ----- Arrange -----
            val retryAfterException = httpException(
                statusCode = 429,
                headers = listOf(HttpHeader(
                    "Retry-After",
                    "5",
                ),),
            )
            every {
                apolloCall.toFlow()
            } returnsMany listOf(
                flowOf(exceptionResponse(retryAfterException)),
                flowOf(dataResponse()),
            )

            // ----- Act -----
            apolloClient.safeQuery(query)

            // ----- Assert -----
            // the computed backoff for the first retry would only be 1 second — the 5-second
            // Retry-After header must be what the virtual clock actually advanced by
            testScheduler.currentTime shouldBe 5_000L
        }

        @Test
        fun `non-transient HTTP 400 is not retried`() = runTest {
            // ----- Arrange -----
            every {
                apolloCall.toFlow()
            } returns flowOf(exceptionResponse(httpException(400)))

            // ----- Act & Assert -----
            shouldThrow<UnexpectedApiException> {
                apolloClient.safeQuery(query)
            }

            verify(exactly = 1) { apolloCall.toFlow() }
        }

        @Test
        fun `OfflineException surfaces immediately without retry`() = runTest {
            // ----- Arrange -----
            every {
                NetworkAvailability.isOnline()
            } returns false
            every {
                apolloCall.toFlow()
            } returns flowOf(exceptionResponse(ApolloNetworkException(message = "no network")))

            // ----- Act & Assert -----
            shouldThrow<OfflineException> {
                apolloClient.safeQuery(
                    query = query,
                    fetchPolicy = FetchPolicy.CacheFirst,
                )
            }

            verify(exactly = 1) { apolloCall.toFlow() }
        }
    }

    @Nested
    inner class SafeMutationDoesNotRetry {
        @Test
        fun `transient 429 during a mutation surfaces immediately without retry`() = runTest {
            // ----- Arrange -----
            coEvery {
                apolloMutationCall.execute()
            } returns mutationExceptionResponse(httpException(429))

            // ----- Act & Assert -----
            shouldThrow<ServerUnavailableException> {
                apolloClient.safeMutation(mutation)
            }

            coVerify(exactly = 1) { apolloMutationCall.execute() }
        }
    }

    @Nested
    inner class SafeQueryFlowRetries {
        @Test
        fun `no data emitted then transient 503 — flow is retried and the success emits normally`() = runTest {
            // ----- Arrange -----
            every {
                apolloCall.toFlow()
            } returnsMany listOf(
                flowOf(exceptionResponse(httpException(503))),
                flowOf(dataResponse()),
            )

            // ----- Act & Assert -----
            apolloClient.safeQueryFlow(query).test {
                awaitItem() shouldBe successData
                awaitComplete()
            }

            // one retry at the base 1-second backoff, no Retry-After header
            testScheduler.currentTime shouldBe 1_000L
        }

        @Test
        fun `data already emitted before a transient 503 is not retried and does not re-emit`() = runTest {
            // ----- Arrange -----
            every {
                apolloCall.toFlow()
            } returns flowOf(
                dataResponse(),
                exceptionResponse(httpException(503)),
            )

            // ----- Act & Assert -----
            apolloClient.safeQueryFlow(query).test {
                awaitItem() shouldBe successData
                awaitComplete()
            }

            // emittedAny guards the retry throw — toFlow() must never be called again
            verify(exactly = 1) { apolloCall.toFlow() }
        }

        @Test
        fun `retries stop after the bound and ServerUnavailableException surfaces`() = runTest {
            // ----- Arrange -----
            every {
                apolloCall.toFlow()
            } returns flowOf(exceptionResponse(httpException(429)))

            // ----- Act & Assert -----
            apolloClient.safeQueryFlow(query).test {
                awaitError().shouldBeInstanceOf<ServerUnavailableException>()
            }

            // one initial attempt plus MAX_TRANSIENT_RETRIES (3) retries
            verify(exactly = 4) { apolloCall.toFlow() }
            // backoffs of 1s + 2s + 4s before the final failed attempt
            testScheduler.currentTime shouldBe 7_000L
        }

        @Test
        fun `HTTP 401 during the flow is not retried and notifies session expired`() = runTest {
            // ----- Arrange -----
            every {
                apolloCall.toFlow()
            } returns flowOf(exceptionResponse(httpException(401)))

            // ----- Act & Assert -----
            apolloClient.safeQueryFlow(query).test {
                awaitError().shouldBeInstanceOf<InvalidTokenException>()
            }

            verify(exactly = 1) { apolloCall.toFlow() }
            verify(exactly = 1) { SessionExpiredNotifier.notifySessionExpired() }
        }
    }
}
