package nl.rhaydus.softcover.core.designsystem.presentation.error

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import kotlin.coroutines.cancellation.CancellationException
import nl.rhaydus.designsystem.util.SnackBarManager
import nl.rhaydus.softcover.core.domain.exception.InvalidTokenException
import nl.rhaydus.softcover.core.domain.exception.OfflineException
import nl.rhaydus.softcover.core.domain.exception.UnexpectedApiException
import nl.rhaydus.softcover.core.domain.logging.AppLog
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ApiFailureHandlingTest {
    @BeforeEach
    fun setUp() {
        mockkObject(SnackBarManager)
        mockkObject(AppLog)

        every {
            SnackBarManager.showSnackbar(title = any())
        } returns Unit

        every {
            AppLog.e(any<Throwable>())
        } returns Unit

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
    inner class SnackbarBehaviour {
        @Test
        fun `OfflineException — shows offline snackbar`() {
            // ----- Arrange -----
            val result = Result.failure<Unit>(OfflineException())

            // ----- Act -----
            result.onApiFailure()

            // ----- Assert -----
            verify(exactly = 1) {
                SnackBarManager.showSnackbar(title = "You're offline. Check your connection and try again.")
            }
        }

        @Test
        fun `UnexpectedApiException — shows generic error snackbar`() {
            // ----- Arrange -----
            val result = Result.failure<Unit>(UnexpectedApiException("x"))

            // ----- Act -----
            result.onApiFailure()

            // ----- Assert -----
            verify(exactly = 1) {
                SnackBarManager.showSnackbar(title = "Something went wrong. Please try again.")
            }
        }

        @Test
        fun `InvalidTokenException — snackbar NOT shown`() {
            // ----- Arrange -----
            val result = Result.failure<Unit>(InvalidTokenException())

            // ----- Act -----
            result.onApiFailure()

            // ----- Assert -----
            verify(exactly = 0) { SnackBarManager.showSnackbar(title = any()) }
        }

        @Test
        fun `non-API RuntimeException — snackbar NOT shown and result returned`() {
            // ----- Arrange -----
            val result = Result.failure<String>(RuntimeException("x"))

            // ----- Act -----
            val returned = result.onApiFailure()

            // ----- Assert -----
            verify(exactly = 0) { SnackBarManager.showSnackbar(title = any()) }
            returned.isFailure shouldBe true
        }
    }

    @Nested
    inner class CancellationBehaviour {
        @Test
        fun `CancellationException — rethrows immediately`() {
            // ----- Arrange -----
            val result = Result.failure<Unit>(CancellationException())

            // ----- Act & Assert -----
            shouldThrow<CancellationException> { result.onApiFailure() }
        }
    }

    @Nested
    inner class SuccessBehaviour {
        @Test
        fun `Result success — snackbar NOT shown and original value returned`() {
            // ----- Arrange -----
            val result = Result.success("ok")

            // ----- Act -----
            val returned = result.onApiFailure()

            // ----- Assert -----
            verify(exactly = 0) { SnackBarManager.showSnackbar(title = any()) }
            returned.getOrNull() shouldBe "ok"
        }
    }

    @Nested
    inner class LoggingBehaviour {
        @Test
        fun `non-null logContext — AppLog e called with throwable and context`() {
            // ----- Arrange -----
            val throwable = OfflineException()
            val result = Result.failure<Unit>(throwable)

            // ----- Act -----
            result.onApiFailure(logContext = "ctx")

            // ----- Assert -----
            verify(exactly = 1) {
                AppLog.e(
                    throwable,
                    "ctx",
                )
            }
        }
    }
}
