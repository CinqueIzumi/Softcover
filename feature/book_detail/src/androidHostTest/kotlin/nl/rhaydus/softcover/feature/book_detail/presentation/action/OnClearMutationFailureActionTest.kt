package nl.rhaydus.softcover.feature.book_detail.presentation.action

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnClearMutationFailureActionTest {
    private lateinit var dependencies: BookDetailDependencies
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        dependencies = mockk(relaxed = true)
        stateFlow = MutableStateFlow(
            BookDetailUiState(
                failedMutationBookIds = setOf(1, 2),
                failedMutationEditionIds = setOf(10, 20),
            ),
        )
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(BookDetailLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    @Nested
    inner class Init {
        @Test
        fun `rejects an action with neither a book id nor an edition id`() {
            // ----- Arrange -----
            val noBookId: Int? = null
            val noEditionId: Int? = null

            // ----- Act & Assert -----
            shouldThrow<IllegalArgumentException> {
                OnClearMutationFailureAction(
                    bookId = noBookId,
                    editionId = noEditionId,
                )
            }
        }
    }

    @Nested
    inner class Execute {
        @Test
        fun `clears only the given book id`() = runTest {
            // ----- Arrange -----
            val action = OnClearMutationFailureAction(bookId = 1)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.failedMutationBookIds shouldBe setOf(2)
            stateFlow.value.failedMutationEditionIds shouldBe setOf(10, 20)
        }

        @Test
        fun `clears only the given edition id`() = runTest {
            // ----- Arrange -----
            val action = OnClearMutationFailureAction(editionId = 10)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.failedMutationBookIds shouldBe setOf(1, 2)
            stateFlow.value.failedMutationEditionIds shouldBe setOf(20)
        }

        @Test
        fun `leaves the failures untouched for an id that was not failing`() = runTest {
            // ----- Arrange -----
            val action = OnClearMutationFailureAction(bookId = 99)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.failedMutationBookIds shouldBe setOf(1, 2)
        }
    }
}
