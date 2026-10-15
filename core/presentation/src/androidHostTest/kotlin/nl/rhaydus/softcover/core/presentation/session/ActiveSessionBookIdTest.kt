package nl.rhaydus.softcover.core.presentation.session

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.Book
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ActiveSessionBookIdTest {
    private lateinit var activeSession: MutableStateFlow<ActiveSession?>
    private lateinit var controller: ActiveSessionController

    @BeforeEach
    fun setUp() {
        activeSession = MutableStateFlow(null)
        controller = mockk<ActiveSessionController>().also { mock ->
            every {
                mock.activeSession
            } returns activeSession
        }
    }

    private fun sessionFor(bookId: Int): ActiveSession = mockk<ActiveSession> {
        every {
            book
        } returns mockk<Book> {
            every {
                id
            } returns bookId
        }
    }

    @Nested
    inner class ActiveSessionBookId {
        @Test
        fun `emits null while no session runs`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val emitted = mutableListOf<Int?>()
            backgroundScope.launch {
                controller.activeSessionBookId().toList(emitted)
            }

            // ----- Act -----
            val result = emitted.toList()

            // ----- Assert -----
            result shouldBe listOf(null)
        }

        @Test
        fun `emits the running session's book id and null again when it ends`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val emitted = mutableListOf<Int?>()
            backgroundScope.launch {
                controller.activeSessionBookId().toList(emitted)
            }

            // ----- Act -----
            activeSession.value = sessionFor(bookId = 7)
            activeSession.value = null

            // ----- Assert -----
            emitted shouldBe listOf(null, 7, null)
        }

        @Test
        fun `does not re-emit when a new session state is for the same book`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val emitted = mutableListOf<Int?>()
            backgroundScope.launch {
                controller.activeSessionBookId().toList(emitted)
            }
            activeSession.value = sessionFor(bookId = 7)

            // ----- Act -----
            activeSession.value = sessionFor(bookId = 7)

            // ----- Assert -----
            emitted shouldBe listOf(null, 7)
        }

        @Test
        fun `emits the new id when the session switches to another book`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val emitted = mutableListOf<Int?>()
            backgroundScope.launch {
                controller.activeSessionBookId().toList(emitted)
            }
            activeSession.value = sessionFor(bookId = 7)

            // ----- Act -----
            activeSession.value = sessionFor(bookId = 9)

            // ----- Assert -----
            emitted shouldBe listOf(null, 7, 9)
        }
    }
}
