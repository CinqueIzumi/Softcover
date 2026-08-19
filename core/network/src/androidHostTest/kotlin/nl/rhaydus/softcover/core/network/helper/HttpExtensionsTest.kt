package nl.rhaydus.softcover.core.network.helper

import com.apollographql.apollo.api.http.HttpRequest
import com.apollographql.apollo.api.http.HttpResponse
import com.apollographql.apollo.exception.ApolloNetworkException
import com.apollographql.apollo.network.http.HttpEngine
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockkObject
import kotlinx.coroutines.test.runTest
import nl.rhaydus.platform.NetworkAvailability
import nl.rhaydus.softcover.core.domain.exception.OfflineException
import nl.rhaydus.softcover.core.domain.exception.ServerUnavailableException
import nl.rhaydus.softcover.core.domain.exception.UnexpectedApiException
import okio.Buffer
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.coroutines.cancellation.CancellationException

private const val TEST_URL = "https://example.test/resource"

class HttpExtensionsTest {
    private class FakeHttpEngine : HttpEngine {
        var executeCallCount = 0
            private set
        var responseToReturn: HttpResponse? = null
        var exceptionToThrow: Throwable? = null

        override suspend fun execute(request: HttpRequest): HttpResponse {
            executeCallCount++

            exceptionToThrow?.let { throw it }

            return responseToReturn
                ?: error("FakeHttpEngine.execute called with no response or exception configured")
        }
    }

    private lateinit var engine: FakeHttpEngine

    @BeforeEach
    fun setUp() {
        engine = FakeHttpEngine()

        mockkObject(NetworkAvailability)
        every {
            NetworkAvailability.isOnline()
        } returns true
    }

    private fun responseWithBody(
        statusCode: Int,
        body: String,
    ): HttpResponse =
        HttpResponse.Builder(statusCode)
            .body(Buffer().writeUtf8(body))
            .build()

    private fun responseWithoutBody(statusCode: Int): HttpResponse = HttpResponse.Builder(statusCode).build()

    @Nested
    inner class Offline {
        @Test
        fun `device offline — throws OfflineException without touching the engine`() = runTest {
            // ----- Arrange -----
            every {
                NetworkAvailability.isOnline()
            } returns false

            // ----- Act & Assert -----
            shouldThrow<OfflineException> {
                engine.safeGetText(TEST_URL)
            }

            engine.executeCallCount shouldBe 0
        }
    }

    @Nested
    inner class SuccessfulResponse {
        @Test
        fun `2xx response returns the body text`() = runTest {
            // ----- Arrange -----
            engine.responseToReturn = responseWithBody(
                200,
                "hello world",
            )

            // ----- Act -----
            val result = engine.safeGetText(TEST_URL)

            // ----- Assert -----
            result shouldBe "hello world"
        }
    }

    @Nested
    inner class TransientFailureStatuses {
        @Test
        fun `HTTP 500 throws ServerUnavailableException`() = runTest {
            // ----- Arrange -----
            engine.responseToReturn = responseWithoutBody(500)

            // ----- Act & Assert -----
            shouldThrow<ServerUnavailableException> {
                engine.safeGetText(TEST_URL)
            }
        }

        @Test
        fun `HTTP 503 throws ServerUnavailableException`() = runTest {
            // ----- Arrange -----
            engine.responseToReturn = responseWithoutBody(503)

            // ----- Act & Assert -----
            shouldThrow<ServerUnavailableException> {
                engine.safeGetText(TEST_URL)
            }
        }

        @Test
        fun `HTTP 408 throws ServerUnavailableException`() = runTest {
            // ----- Arrange -----
            engine.responseToReturn = responseWithoutBody(408)

            // ----- Act & Assert -----
            shouldThrow<ServerUnavailableException> {
                engine.safeGetText(TEST_URL)
            }
        }

        @Test
        fun `HTTP 429 throws ServerUnavailableException`() = runTest {
            // ----- Arrange -----
            engine.responseToReturn = responseWithoutBody(429)

            // ----- Act & Assert -----
            shouldThrow<ServerUnavailableException> {
                engine.safeGetText(TEST_URL)
            }
        }
    }

    @Nested
    inner class NonTransientFailureStatuses {
        @Test
        fun `HTTP 404 throws UnexpectedApiException`() = runTest {
            // ----- Arrange -----
            engine.responseToReturn = responseWithoutBody(404)

            // ----- Act & Assert -----
            shouldThrow<UnexpectedApiException> {
                engine.safeGetText(TEST_URL)
            }
        }

        @Test
        fun `HTTP 400 throws UnexpectedApiException`() = runTest {
            // ----- Arrange -----
            engine.responseToReturn = responseWithoutBody(400)

            // ----- Act & Assert -----
            shouldThrow<UnexpectedApiException> {
                engine.safeGetText(TEST_URL)
            }
        }
    }

    @Nested
    inner class NullBody {
        @Test
        fun `2xx response with a null body throws UnexpectedApiException`() = runTest {
            // ----- Arrange -----
            engine.responseToReturn = responseWithoutBody(200)

            // ----- Act & Assert -----
            shouldThrow<UnexpectedApiException> {
                engine.safeGetText(TEST_URL)
            }
        }
    }

    @Nested
    inner class TransportFailureMapping {
        @Test
        fun `ApolloNetworkException with device offline — throws OfflineException`() = runTest {
            // ----- Arrange -----
            // First call is the pre-check (online, so the engine gets invoked); the second call is the
            // exception-mapping check once the engine's own network access has failed.
            every {
                NetworkAvailability.isOnline()
            } returnsMany listOf(true, false)
            engine.exceptionToThrow = ApolloNetworkException(message = "connection failed")

            // ----- Act & Assert -----
            shouldThrow<OfflineException> {
                engine.safeGetText(TEST_URL)
            }
        }

        @Test
        fun `ApolloNetworkException with device online — throws ServerUnavailableException`() = runTest {
            // ----- Arrange -----
            engine.exceptionToThrow = ApolloNetworkException(message = "connection failed")

            // ----- Act & Assert -----
            shouldThrow<ServerUnavailableException> {
                engine.safeGetText(TEST_URL)
            }
        }
    }

    @Nested
    inner class Cancellation {
        @Test
        fun `CancellationException from the engine propagates untouched`() = runTest {
            // ----- Arrange -----
            engine.exceptionToThrow = CancellationException("cancelled")

            // ----- Act & Assert -----
            val exception = shouldThrow<CancellationException> {
                engine.safeGetText(TEST_URL)
            }

            exception.message shouldBe "cancelled"
        }
    }
}
