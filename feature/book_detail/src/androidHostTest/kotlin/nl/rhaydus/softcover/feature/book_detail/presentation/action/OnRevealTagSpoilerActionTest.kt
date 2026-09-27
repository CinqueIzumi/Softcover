package nl.rhaydus.softcover.feature.book_detail.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.collections.immutable.persistentSetOf
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

class OnRevealTagSpoilerActionTest {
    private lateinit var dependencies: BookDetailDependencies
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var localVariablesFlow: MutableStateFlow<BookDetailLocalVariables>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        dependencies = mockk(relaxed = true)
        stateFlow = MutableStateFlow(BookDetailUiState())
        localVariablesFlow = MutableStateFlow(BookDetailLocalVariables())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = localVariablesFlow,
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    @Nested
    inner class Execute {
        @Test
        fun `adds the key to revealedTagKeys when the set was empty`() = runTest {
            // ----- Arrange -----
            val key = "5"
            stateFlow.value = BookDetailUiState(revealedTagKeys = persistentSetOf())
            val action = OnRevealTagSpoilerAction(key = key)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.revealedTagKeys shouldBe setOf("5")
        }

        @Test
        fun `adds the key to revealedTagKeys without removing existing keys`() = runTest {
            // ----- Arrange -----
            val existingKey = "3"
            val newKey = "7"
            stateFlow.value = BookDetailUiState(revealedTagKeys = persistentSetOf(existingKey))
            val action = OnRevealTagSpoilerAction(key = newKey)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.revealedTagKeys shouldBe setOf(existingKey, newKey)
        }

        @Test
        fun `does not duplicate a key already present in the set`() = runTest {
            // ----- Arrange -----
            val key = "11"
            stateFlow.value = BookDetailUiState(revealedTagKeys = persistentSetOf(key))
            val action = OnRevealTagSpoilerAction(key = key)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.revealedTagKeys shouldBe setOf(key)
        }

        @Test
        fun `preserves all previously revealed keys when adding a new one`() = runTest {
            // ----- Arrange -----
            val existing = persistentSetOf("1", "2", "3")
            val newKey = "4"
            stateFlow.value = BookDetailUiState(revealedTagKeys = existing)
            val action = OnRevealTagSpoilerAction(key = newKey)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.revealedTagKeys shouldBe setOf("1", "2", "3", "4")
        }

        @Test
        fun `does not alter any other fields in the state`() = runTest {
            // ----- Arrange -----
            val initialState = BookDetailUiState(
                loadingBookDetails = true,
                revealedTagKeys = persistentSetOf(),
            )
            stateFlow.value = initialState
            val action = OnRevealTagSpoilerAction(key = "9")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.loadingBookDetails shouldBe true
        }
    }
}
