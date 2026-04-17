package nl.rhaydus.softcover.feature.reading.presentation.action

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.books.domain.usecase.MarkBookAsReadUseCase
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenDependencies
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnMarkBookAsReadClickActionTest {

    private lateinit var markBookAsReadUseCase: MarkBookAsReadUseCase
    private lateinit var dependencies: ReadingScreenDependencies
    private lateinit var stateFlow: MutableStateFlow<ReadingScreenUiState>
    private lateinit var scope: ActionScope<ReadingScreenUiState, ReadingScreenEvent, ReadingLocalVariables>

    @BeforeEach
    fun setUp() {
        markBookAsReadUseCase = mockk()
        stateFlow = MutableStateFlow(ReadingScreenUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(ReadingLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
        dependencies = mockk<ReadingScreenDependencies>(relaxed = true).also { mock ->
            every {
                mock.markBookAsReadUseCase
            } returns markBookAsReadUseCase
        }
    }

    private fun stubBook(): Book = mockk()

    @Nested
    inner class Execute {

        @Test
        fun `invokes markBookAsReadUseCase with the provided book`() = runTest {
            // ----- Arrange -----
            val book = stubBook()

            coEvery {
                markBookAsReadUseCase(book = book)
            } returns Result.success(Unit)

            val action = OnMarkBookAsReadClickAction(book = book)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                markBookAsReadUseCase(book = book)
            }
        }

        @Test
        fun `does not throw when use case succeeds`() = runTest {
            // ----- Arrange -----
            val book = stubBook()

            coEvery {
                markBookAsReadUseCase(book = book)
            } returns Result.success(Unit)

            val action = OnMarkBookAsReadClickAction(book = book)

            // ----- Act & Assert -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )
        }

        @Test
        fun `does not throw when use case fails`() = runTest {
            // ----- Arrange -----
            val book = stubBook()

            coEvery {
                markBookAsReadUseCase(book = book)
            } returns Result.failure(RuntimeException("network error"))

            val action = OnMarkBookAsReadClickAction(book = book)

            // ----- Act & Assert -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )
        }
    }
}
