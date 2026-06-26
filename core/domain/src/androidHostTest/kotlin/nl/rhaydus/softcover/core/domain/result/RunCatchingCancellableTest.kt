package nl.rhaydus.softcover.core.domain.result

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlin.coroutines.cancellation.CancellationException
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class RunCatchingCancellableTest {
    @Nested
    inner class SuccessBehaviour {
        @Test
        fun `block returns value — result is success with that value`() {
            // ----- Arrange -----
            // (no setup needed)

            // ----- Act -----
            val result = runCatchingCancellable { "ok" }

            // ----- Assert -----
            result.isSuccess shouldBe true
            result.getOrNull() shouldBe "ok"
        }
    }

    @Nested
    inner class NonCancellationFailureBehaviour {
        @Test
        fun `block throws RuntimeException — result is failure holding that exception`() {
            // ----- Arrange -----
            val throwable = RuntimeException("boom")

            // ----- Act -----
            val result = runCatchingCancellable { throw throwable }

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe throwable
        }
    }

    @Nested
    inner class CancellationBehaviour {
        @Test
        fun `block throws CancellationException — exception is rethrown, not captured`() {
            // ----- Arrange -----
            // (no setup needed)

            // ----- Act & Assert -----
            shouldThrow<CancellationException> {
                runCatchingCancellable { throw CancellationException() }
            }
        }
    }
}
