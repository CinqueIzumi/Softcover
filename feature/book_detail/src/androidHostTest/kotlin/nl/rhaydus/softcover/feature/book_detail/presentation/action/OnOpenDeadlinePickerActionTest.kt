package nl.rhaydus.softcover.feature.book_detail.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.domain.model.BookDeadline
import nl.rhaydus.softcover.core.domain.model.DeadlineUnit
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnOpenDeadlinePickerActionTest {
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

    private fun stubDependencies(testScope: TestScope): BookDetailDependencies {
        val dispatcher = UnconfinedTestDispatcher(testScope.testScheduler)
        return mockk<BookDetailDependencies>(relaxed = true).also { mock ->
            every {
                mock.coroutineScope
            } returns testScope

            every {
                mock.mainDispatcher
            } returns dispatcher

            every {
                mock.observeCurrentDateUseCase()
            } returns flowOf(
                LocalDate(
                    2026,
                    9,
                    30,
                ),
            )

            every {
                mock.launch(any())
            } answers { callOriginal() }
        }
    }

    @Nested
    inner class Execute {
        @Test
        fun `opens the picker on today when there is no deadline`() = runTest {
            // ----- Arrange -----
            val today = LocalDate(
                2026,
                9,
                30,
            )
            dependencies = stubDependencies(this)
            every {
                dependencies.observeCurrentDateUseCase()
            } returns flowOf(today)
            val action = OnOpenDeadlinePickerAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.deadlinePicker?.initialDate shouldBe today
        }

        @Test
        fun `opens the picker on the existing deadline date when one is set`() = runTest {
            // ----- Arrange -----
            val deadlineDate = LocalDate(
                2026,
                12,
                24,
            )
            stateFlow.value = BookDetailUiState(
                deadline = BookDeadline(
                    bookId = 1,
                    deadlineDate = deadlineDate,
                    setAt = LocalDate(
                        2026,
                        9,
                        1,
                    ),
                    initialPerDay = 10f,
                    unit = DeadlineUnit.PAGES,
                ),
            )
            dependencies = stubDependencies(this)
            every {
                dependencies.observeCurrentDateUseCase()
            } returns flowOf(
                LocalDate(
                    2026,
                    9,
                    30,
                ),
            )
            val action = OnOpenDeadlinePickerAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.deadlinePicker?.initialDate shouldBe deadlineDate
        }

        @Test
        fun `leaves the picker closed when today cannot be resolved and there is no deadline`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)
            every {
                dependencies.observeCurrentDateUseCase()
            } returns emptyFlow()
            val action = OnOpenDeadlinePickerAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.deadlinePicker shouldBe null
        }

        @Test
        fun `sets fabMenuExpanded to false`() = runTest {
            // ----- Arrange -----
            stateFlow.value = BookDetailUiState(fabMenuExpanded = true)
            dependencies = stubDependencies(this)
            val action = OnOpenDeadlinePickerAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.fabMenuExpanded shouldBe false
        }

        @Test
        fun `does not change other state fields`() = runTest {
            // ----- Arrange -----
            stateFlow.value = BookDetailUiState(loadingBookDetails = false)
            dependencies = stubDependencies(this)
            val action = OnOpenDeadlinePickerAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.loadingBookDetails shouldBe false
        }
    }
}
