package nl.rhaydus.softcover.core.network.helper

import com.apollographql.apollo.api.http.HttpHeader
import com.apollographql.apollo.exception.ApolloHttpException
import io.kotest.matchers.shouldBe
import okio.Buffer
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.seconds

class RateLimitHeadersTest {
    private fun exceptionWithRetryAfter(value: String): ApolloHttpException = ApolloHttpException(
        statusCode = HTTP_TOO_MANY_REQUESTS,
        headers = listOf(
            HttpHeader(
                "Retry-After",
                value,
            ),
        ),
        body = Buffer(),
        message = "HTTP $HTTP_TOO_MANY_REQUESTS",
    )

    @Nested
    inner class RetryAfterOrNull {
        @Test
        fun `a value above the maximum wait is clamped to 60 seconds`() {
            // ----- Arrange -----
            val exception: ApolloHttpException = exceptionWithRetryAfter("1700000000")

            // ----- Act -----
            val result = exception.retryAfterOrNull()

            // ----- Assert -----
            result shouldBe 60.seconds
        }

        @Test
        fun `a negative value becomes zero`() {
            // ----- Arrange -----
            val exception: ApolloHttpException = exceptionWithRetryAfter("-10")

            // ----- Act -----
            val result = exception.retryAfterOrNull()

            // ----- Assert -----
            result shouldBe 0.seconds
        }

        @Test
        fun `a value within range is kept`() {
            // ----- Arrange -----
            val exception: ApolloHttpException = exceptionWithRetryAfter("30")

            // ----- Act -----
            val result = exception.retryAfterOrNull()

            // ----- Assert -----
            result shouldBe 30.seconds
        }
    }
}
