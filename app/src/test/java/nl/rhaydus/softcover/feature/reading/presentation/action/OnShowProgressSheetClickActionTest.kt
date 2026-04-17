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

class OnShowProgressSheetClickActionTest {

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
        fun `sets bookToUpdate to the provided book`() = runTest {
            // ----- Arrange -----
            val book = stubBook()
            val action = OnShowProgressSheetClickAction(book = book)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.bookToUpdate shouldBe book
        }

        @Test
        fun `sets showProgressSheet to true`() = runTest {
            // ----- Arrange -----
            val book = stubBook()
            stateFlow.value = ReadingScreenUiState(showProgressSheet = false)
            val action = OnShowProgressSheetClickAction(book = book)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.showProgressSheet shouldBe true
        }

        @Test
        fun `replaces previously stored bookToUpdate with the new book`() = runTest {
            // ----- Arrange -----
            val previousBook = stubBook()
            val newBook = stubBook()
            stateFlow.value = ReadingScreenUiState(bookToUpdate = previousBook)
            val action = OnShowProgressSheetClickAction(book = newBook)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.bookToUpdate shouldBe newBook
        }

        @Test
        fun `preserves all other state fields unchanged`() = runTest {
            // ----- Arrange -----
            val book = stubBook()
            stateFlow.value = ReadingScreenUiState(
                isLoading = false,
                showProgressSheet = false,
            )
            val action = OnShowProgressSheetClickAction(book = book)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isLoading shouldBe false
        }
    }
}
