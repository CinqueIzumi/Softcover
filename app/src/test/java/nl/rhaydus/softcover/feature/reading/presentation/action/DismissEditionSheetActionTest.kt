package nl.rhaydus.softcover.feature.reading.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenDependencies
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class DismissEditionSheetActionTest {

    private lateinit var dependencies: ReadingScreenDependencies
    private lateinit var stateFlow: MutableStateFlow<ReadingScreenUiState>
    private lateinit var scope: ActionScope<ReadingScreenUiState, ReadingScreenEvent, ReadingLocalVariables>

    @BeforeEach
    fun setUp() {
        dependencies = mockk(relaxed = true)
        stateFlow = MutableStateFlow(ReadingScreenUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(ReadingLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    private fun stubBook(): Book = mockk()

    @Nested
    inner class Execute {

        @Test
        fun `sets bookToUpdate to null after execute`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ReadingScreenUiState(bookToUpdate = stubBook())

            // ----- Act -----
            DismissEditionSheetAction.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.bookToUpdate shouldBe null
        }

        @Test
        fun `sets showEditionSheet to false after execute`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ReadingScreenUiState(showEditionSheet = true)

            // ----- Act -----
            DismissEditionSheetAction.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.showEditionSheet shouldBe false
        }

        @Test
        fun `clears bookToUpdate and showEditionSheet when both were set`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ReadingScreenUiState(
                bookToUpdate = stubBook(),
                showEditionSheet = true,
            )

            // ----- Act -----
            DismissEditionSheetAction.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.bookToUpdate shouldBe null
            stateFlow.value.showEditionSheet shouldBe false
        }

        @Test
        fun `preserves all other state fields unchanged`() = runTest {
            // ----- Arrange -----
            val existingBooks = listOf(stubBook(), stubBook())
            stateFlow.value = ReadingScreenUiState(
                books = existingBooks,
                isLoading = false,
                bookToUpdate = stubBook(),
                showEditionSheet = true,
                showProgressSheet = true,
            )

            // ----- Act -----
            DismissEditionSheetAction.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.books shouldBe existingBooks
            stateFlow.value.isLoading shouldBe false
            stateFlow.value.showProgressSheet shouldBe true
        }
    }
}
