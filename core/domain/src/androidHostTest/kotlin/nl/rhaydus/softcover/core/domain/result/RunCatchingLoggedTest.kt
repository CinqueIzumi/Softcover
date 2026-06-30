package nl.rhaydus.softcover.core.domain.result

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import kotlin.coroutines.cancellation.CancellationException
import nl.rhaydus.softcover.core.domain.exception.OfflineException
import nl.rhaydus.softcover.core.domain.logging.AppLog
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class RunCatchingLoggedTest {
    @BeforeEach
    fun setUp() {
        mockkObject(AppLog)

        every {
            AppLog.e(
                any<Throwable>(),
                any<String>(),
            )
        } returns Unit
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Nested
    inner class SuccessBehaviour {
        @Test
        fun `block returns value — result is success and AppLog e is never called`() {
            // ----- Arrange -----
            // (no setup needed)

            // ----- Act -----
            val result = runCatchingLogged { "ok" }

            // ----- Assert -----
            result.isSuccess shouldBe true
            result.getOrNull() shouldBe "ok"
            verify(exactly = 0) { AppLog.e(
                any<Throwable>(),
                any<String>(),
            ) }
        }
    }

    @Nested
    inner class FailureWithContextBehaviour {
        @Test
        fun `block throws with explicit context — result is failure and AppLog e called with throwable and context`() {
            // ----- Arrange -----
            val throwable = OfflineException()

            // ----- Act -----
            val result = runCatchingLogged(context = "ctx") { throw throwable }

            // ----- Assert -----
            result.isFailure shouldBe true
            verify(exactly = 1) {
                AppLog.e(
                    throwable,
                    "ctx",
                )
            }
        }
    }

    @Nested
    inner class FailureWithoutContextBehaviour {
        @Test
        fun `block throws, null context, throwable has message — AppLog e called with throwable message`() {
            // ----- Arrange -----
            val throwable = RuntimeException("boom")

            // ----- Act -----
            runCatchingLogged { throw throwable }

            // ----- Assert -----
            verify(exactly = 1) {
                AppLog.e(
                    throwable,
                    "boom",
                )
            }
        }

        @Test
        fun `block throws, null context, throwable message is null — AppLog e called with throwable toString`() {
            // ----- Arrange -----
            val throwable = RuntimeException()

            // ----- Act -----
            runCatchingLogged { throw throwable }

            // ----- Assert -----
            verify(exactly = 1) {
                AppLog.e(
                    throwable,
                    throwable.toString(),
                )
            }
        }
    }

    @Nested
    inner class ReturnsBehaviourOnFailure {
        @Test
        fun `block throws — returned Result is failure holding the thrown throwable`() {
            // ----- Arrange -----
            val throwable = OfflineException()

            // ----- Act -----
            val result = runCatchingLogged { throw throwable }

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe throwable
        }
    }

    @Nested
    inner class CancellationBehaviour {
        @Test
        fun `block throws CancellationException — exception is rethrown and AppLog e is never called`() {
            // ----- Arrange -----
            // (no setup needed)

            // ----- Act & Assert -----
            shouldThrow<CancellationException> {
                runCatchingLogged { throw CancellationException() }
            }
            verify(exactly = 0) { AppLog.e(
                any<Throwable>(),
                any<String>(),
            ) }
        }
    }
}
