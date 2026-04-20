package nl.rhaydus.softcover.feature.book_detail.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnDismissDeadlinePickerActionTest {

    private lateinit var dependencies: BookDetailDependencies
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        stateFlow = MutableStateFlow(BookDetailUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(BookDetailLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    private fun stubDependencies(testScope: kotlinx.coroutines.test.TestScope): BookDetailDependencies {
        val dispatcher = UnconfinedTestDispatcher(testScope.testScheduler)
        return mockk<BookDetailDependencies>(relaxed = true).also { mock ->
            every {
                mock.coroutineScope
            } returns testScope

            every {
                mock.mainDispatcher
            } returns dispatcher

            every {
                mock.launch(any())
            } answers { callOriginal() }
        }
    }

    @Nested
    inner class Execute {

        @Test
        fun `sets showDeadlinePicker to false`() = runTest {
            // ----- Arrange -----
            stateFlow.value = BookDetailUiState(showDeadlinePicker = true)
            dependencies = stubDependencies(this)
            val action = OnDismissDeadlinePickerAction()

            // ----- Act -----
            action.execute(dependencies = dependencies, scope = scope)

            // ----- Assert -----
            stateFlow.value.showDeadlinePicker shouldBe false
        }

        @Test
        fun `does not change other state fields`() = runTest {
            // ----- Arrange -----
            stateFlow.value = BookDetailUiState(showDeadlinePicker = true, loadingBookDetails = false)
            dependencies = stubDependencies(this)
            val action = OnDismissDeadlinePickerAction()

            // ----- Act -----
            action.execute(dependencies = dependencies, scope = scope)

            // ----- Assert -----
            stateFlow.value.loadingBookDetails shouldBe false
        }
    }
}
