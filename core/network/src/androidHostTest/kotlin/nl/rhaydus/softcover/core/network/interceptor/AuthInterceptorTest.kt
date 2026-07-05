package nl.rhaydus.softcover.core.network.interceptor

import com.apollographql.apollo.api.ApolloRequest
import com.apollographql.apollo.interceptor.ApolloInterceptorChain
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.GetUserBookListsQuery
import nl.rhaydus.softcover.core.domain.auth.AuthTokenProvider
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class AuthInterceptorTest {
    private lateinit var authTokenProvider: AuthTokenProvider
    private lateinit var chain: ApolloInterceptorChain

    // Real ApolloRequest built from a concrete generated operation — newBuilder() works without mocking.
    private val baseRequest: ApolloRequest<GetUserBookListsQuery.Data> =
        ApolloRequest.Builder(GetUserBookListsQuery()).build()

    @BeforeEach
    fun setUp() {
        authTokenProvider = mockk()
        chain = mockk()

        @Suppress("UNCHECKED_CAST")
        every {
            chain.proceed(any<ApolloRequest<GetUserBookListsQuery.Data>>())
        } returns emptyFlow()
    }

    private fun buildInterceptor(token: String?): AuthInterceptor {
        every {
            authTokenProvider.apiKey
        } returns flowOf(token)
        return AuthInterceptor(authTokenProvider = authTokenProvider)
    }

    @Nested
    inner class Intercept {
        @Test
        fun `adds Authorization Bearer header when source emits a non-blank key`() = runTest {
            // ----- Arrange -----
            val interceptor = buildInterceptor("my-secret-key")
            val capturedRequest = slot<ApolloRequest<GetUserBookListsQuery.Data>>()

            @Suppress("UNCHECKED_CAST")
            every {
                chain.proceed(capture(capturedRequest))
            } returns emptyFlow()

            // ----- Act -----
            interceptor.intercept(
                baseRequest,
                chain,
            ).collect {}

            // ----- Assert -----
            val authHeader = capturedRequest.captured.httpHeaders
                ?.firstOrNull { it.name == "Authorization" }
            authHeader?.value shouldBe "Bearer my-secret-key"
        }

        @Test
        fun `forwards request unchanged when source emits null`() = runTest {
            // ----- Arrange -----
            val interceptor = buildInterceptor(null)
            val capturedRequest = slot<ApolloRequest<GetUserBookListsQuery.Data>>()

            @Suppress("UNCHECKED_CAST")
            every {
                chain.proceed(capture(capturedRequest))
            } returns emptyFlow()

            // ----- Act -----
            interceptor.intercept(
                baseRequest,
                chain,
            ).collect {}

            // ----- Assert -----
            val authHeader = capturedRequest.captured.httpHeaders
                ?.firstOrNull { it.name == "Authorization" }
            authHeader shouldBe null
        }

        @Test
        fun `forwards request unchanged when source emits blank string`() = runTest {
            // ----- Arrange -----
            val interceptor = buildInterceptor("   ")
            val capturedRequest = slot<ApolloRequest<GetUserBookListsQuery.Data>>()

            @Suppress("UNCHECKED_CAST")
            every {
                chain.proceed(capture(capturedRequest))
            } returns emptyFlow()

            // ----- Act -----
            interceptor.intercept(
                baseRequest,
                chain,
            ).collect {}

            // ----- Assert -----
            val authHeader = capturedRequest.captured.httpHeaders
                ?.firstOrNull { it.name == "Authorization" }
            authHeader shouldBe null
        }

        @Test
        fun `treats empty string as blank and forwards request unchanged`() = runTest {
            // ----- Arrange -----
            val interceptor = buildInterceptor("")
            val capturedRequest = slot<ApolloRequest<GetUserBookListsQuery.Data>>()

            @Suppress("UNCHECKED_CAST")
            every {
                chain.proceed(capture(capturedRequest))
            } returns emptyFlow()

            // ----- Act -----
            interceptor.intercept(
                baseRequest,
                chain,
            ).collect {}

            // ----- Assert -----
            val authHeader = capturedRequest.captured.httpHeaders
                ?.firstOrNull { it.name == "Authorization" }
            authHeader shouldBe null
        }
    }

    @Nested
    inner class FreshTokenRead {
        @Test
        fun `reads token fresh on every request — token change between calls is reflected`() = runTest {
            // ----- Arrange -----
            every {
                authTokenProvider.apiKey
            } returnsMany listOf(flowOf("first-key"), flowOf("second-key"))
            val interceptor = AuthInterceptor(authTokenProvider = authTokenProvider)

            val captured1 = slot<ApolloRequest<GetUserBookListsQuery.Data>>()

            @Suppress("UNCHECKED_CAST")
            every {
                chain.proceed(capture(captured1))
            } returns emptyFlow()

            // ----- Act — first intercept reads "first-key" -----
            interceptor.intercept(
                baseRequest,
                chain,
            ).collect {}

            val captured2 = slot<ApolloRequest<GetUserBookListsQuery.Data>>()

            @Suppress("UNCHECKED_CAST")
            every {
                chain.proceed(capture(captured2))
            } returns emptyFlow()

            // ----- Act — second intercept reads "second-key" fresh from the provider -----
            interceptor.intercept(
                baseRequest,
                chain,
            ).collect {}

            // ----- Assert -----
            val header1 = captured1.captured.httpHeaders?.firstOrNull { it.name == "Authorization" }
            val header2 = captured2.captured.httpHeaders?.firstOrNull { it.name == "Authorization" }
            header1?.value shouldBe "Bearer first-key"
            header2?.value shouldBe "Bearer second-key"
        }
    }
}
