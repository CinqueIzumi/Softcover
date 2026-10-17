package nl.rhaydus.softcover.feature.library.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.book.domain.usecase.ReorderShelfBooksUseCase
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnReorderShelfBooksActionTest {
    private lateinit var reorderShelfBooksUseCase: ReorderShelfBooksUseCase
    private lateinit var stateFlow: MutableStateFlow<LibraryUiState>
    private lateinit var scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>

    @BeforeEach
    fun setUp() {
        reorderShelfBooksUseCase = mockk(relaxed = true)
        stateFlow = MutableStateFlow(LibraryUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LibraryLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    private fun stubDependencies(): LibraryDependencies =
        mockk<LibraryDependencies>(relaxed = true).also { mock ->
            every {
                mock.reorderShelfBooksUseCase
            } returns reorderShelfBooksUseCase
        }

    @Nested
    inner class Execute {
        @Test
        fun `invokes reorderShelfBooksUseCase with the given status and bookIds`() = runTest {
            // ----- Arrange -----
            val status = UserBookStatus.READ
            val bookIds = listOf(3, 1, 2)
            val action = OnReorderShelfBooksAction(
                status = status,
                prefixOrderedBookIds = bookIds,
            )

            // ----- Act -----
            action.execute(
                dependencies = stubDependencies(),
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                reorderShelfBooksUseCase(
                    status = status,
                    prefixOrderedBookIds = bookIds,
                )
            }
        }

        @Test
        fun `does not throw and does not mutate state when the use case returns failure`() = runTest {
            // ----- Arrange -----
            val status = UserBookStatus.WANT_TO_READ
            val bookIds = listOf(10, 20)
            val action = OnReorderShelfBooksAction(
                status = status,
                prefixOrderedBookIds = bookIds,
            )
            val stateBefore = stateFlow.value

            coEvery {
                reorderShelfBooksUseCase(
                    status = any(),
                    prefixOrderedBookIds = any(),
                )
            } returns Result.failure(RuntimeException("db write failed"))

            // ----- Act -----
            action.execute(
                dependencies = stubDependencies(),
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value shouldBe stateBefore
        }
    }
}
