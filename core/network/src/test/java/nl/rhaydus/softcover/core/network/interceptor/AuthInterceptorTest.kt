package nl.rhaydus.softcover.core.network.interceptor

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.MutableStateFlow
import nl.rhaydus.softcover.core.domain.auth.AuthTokenProvider
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class AuthInterceptorTest {
    private lateinit var apiKeyFlow: MutableStateFlow<String?>
    private lateinit var authTokenProvider: AuthTokenProvider
    private lateinit var chain: Interceptor.Chain
    private lateinit var mockResponse: Response

    private val baseRequest: Request = Request.Builder()
        .url("https://example.com")
        .build()

    @BeforeEach
    fun setUp() {
        apiKeyFlow = MutableStateFlow(null)
        authTokenProvider = mockk()

        every {
            authTokenProvider.apiKey
        } returns apiKeyFlow

        chain = mockk()
        mockResponse = mockk()

        every {
            chain.request()
        } returns baseRequest

        every {
            chain.proceed(any())
        } returns mockResponse
    }

    private fun buildInterceptor(): AuthInterceptor {
        val interceptor = AuthInterceptor(authTokenProvider = authTokenProvider)
        // Allow the IO coroutine launched in init to process the current MutableStateFlow value
        Thread.sleep(50)
        return interceptor
    }

    @Nested
    inner class Intercept {
        @Test
        fun `adds Authorization Bearer header when source emits a non-blank key`() {
            // ----- Arrange -----
            apiKeyFlow.value = "my-secret-key"
            val interceptor = buildInterceptor()
            val capturedRequest = slot<Request>()

            every {
                chain.proceed(capture(capturedRequest))
            } returns mockResponse

            // ----- Act -----
            interceptor.intercept(chain)

            // ----- Assert -----
            capturedRequest.captured.header("Authorization") shouldBe "Bearer my-secret-key"
        }

        @Test
        fun `forwards request unchanged when source emits null`() {
            // ----- Arrange -----
            apiKeyFlow.value = null
            val interceptor = buildInterceptor()
            val capturedRequest = slot<Request>()

            every {
                chain.proceed(capture(capturedRequest))
            } returns mockResponse

            // ----- Act -----
            interceptor.intercept(chain)

            // ----- Assert -----
            capturedRequest.captured.header("Authorization") shouldBe null
        }

        @Test
        fun `forwards request unchanged when source emits blank string`() {
            // ----- Arrange -----
            apiKeyFlow.value = "   "
            val interceptor = buildInterceptor()
            val capturedRequest = slot<Request>()

            every {
                chain.proceed(capture(capturedRequest))
            } returns mockResponse

            // ----- Act -----
            interceptor.intercept(chain)

            // ----- Assert -----
            capturedRequest.captured.header("Authorization") shouldBe null
        }

        @Test
        fun `treats empty string as blank and forwards request unchanged`() {
            // ----- Arrange -----
            apiKeyFlow.value = ""
            val interceptor = buildInterceptor()
            val capturedRequest = slot<Request>()

            every {
                chain.proceed(capture(capturedRequest))
            } returns mockResponse

            // ----- Act -----
            interceptor.intercept(chain)

            // ----- Assert -----
            capturedRequest.captured.header("Authorization") shouldBe null
        }
    }

    @Nested
    inner class TokenObservation {
        @Test
        fun `updates header when flow emits a new distinct key after construction`() {
            // ----- Arrange -----
            apiKeyFlow.value = "initial-key"
            val interceptor = buildInterceptor()

            apiKeyFlow.value = "updated-key"
            Thread.sleep(50)

            val capturedRequest = slot<Request>()

            every {
                chain.proceed(capture(capturedRequest))
            } returns mockResponse

            // ----- Act -----
            interceptor.intercept(chain)

            // ----- Assert -----
            capturedRequest.captured.header("Authorization") shouldBe "Bearer updated-key"
        }

        @Test
        fun `clears header when flow transitions from a valid key to null`() {
            // ----- Arrange -----
            apiKeyFlow.value = "some-key"
            val interceptor = buildInterceptor()

            apiKeyFlow.value = null
            Thread.sleep(50)

            val capturedRequest = slot<Request>()

            every {
                chain.proceed(capture(capturedRequest))
            } returns mockResponse

            // ----- Act -----
            interceptor.intercept(chain)

            // ----- Assert -----
            capturedRequest.captured.header("Authorization") shouldBe null
        }

        @Test
        fun `clears header when flow transitions from a valid key to blank`() {
            // ----- Arrange -----
            apiKeyFlow.value = "some-key"
            val interceptor = buildInterceptor()

            apiKeyFlow.value = "  "
            Thread.sleep(50)

            val capturedRequest = slot<Request>()

            every {
                chain.proceed(capture(capturedRequest))
            } returns mockResponse

            // ----- Act -----
            interceptor.intercept(chain)

            // ----- Assert -----
            capturedRequest.captured.header("Authorization") shouldBe null
        }
    }
}
