package nl.rhaydus.softcover.core.presentation.error

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.domain.exception.InvalidTokenException
import nl.rhaydus.softcover.core.domain.exception.OfflineException
import nl.rhaydus.softcover.core.domain.exception.ServerUnavailableException
import nl.rhaydus.softcover.core.domain.exception.UnexpectedApiException

class ApiErrorMessageTest {
    @Nested
    inner class ToUserMessage {
        @Test
        fun `OfflineException maps to offline copy`() {
            // ----- Arrange -----
            val throwable = OfflineException()

            // ----- Act -----
            val result = throwable.toUserMessage()

            // ----- Assert -----
            result shouldBe "You're offline. Check your connection and try again."
        }

        @Test
        fun `ServerUnavailableException maps to server unavailable copy`() {
            // ----- Arrange -----
            val throwable = ServerUnavailableException("503")

            // ----- Act -----
            val result = throwable.toUserMessage()

            // ----- Assert -----
            result shouldBe "The server is unavailable right now. Please try again."
        }

        @Test
        fun `UnexpectedApiException maps to generic error copy`() {
            // ----- Arrange -----
            val throwable = UnexpectedApiException("any")

            // ----- Act -----
            val result = throwable.toUserMessage()

            // ----- Assert -----
            result shouldBe "Something went wrong. Please try again."
        }

        @Test
        fun `InvalidTokenException maps to null`() {
            // ----- Arrange -----
            val throwable = InvalidTokenException()

            // ----- Act -----
            val result = throwable.toUserMessage()

            // ----- Assert -----
            result shouldBe null
        }

        @Test
        fun `arbitrary RuntimeException maps to null`() {
            // ----- Arrange -----
            val throwable = RuntimeException("x")

            // ----- Act -----
            val result = throwable.toUserMessage()

            // ----- Assert -----
            result shouldBe null
        }
    }
}
