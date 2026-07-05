package nl.rhaydus.softcover.feature.book_detail.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.designsystem.presentation.model.ProgressSheetTab
import nl.rhaydus.softcover.core.domain.model.ProgressUnit
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnProgressTabClickActionTest {
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
                mock.launch(any())
            } answers { callOriginal() }
        }
    }

    @Nested
    inner class Execute {
        @Test
        fun `sets selectedProgressSheetTab to PAGE when tab is PAGE`() = runTest {
            // ----- Arrange -----
            stateFlow.value = stateFlow.value.copy(selectedProgressSheetTab = ProgressSheetTab.PERCENTAGE)
            dependencies = stubDependencies(this)
            val action = OnProgressTabClickAction(tab = ProgressSheetTab.PAGE)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.selectedProgressSheetTab shouldBe ProgressSheetTab.PAGE
        }

        @Test
        fun `sets selectedProgressSheetTab to PERCENTAGE when tab is PERCENTAGE`() = runTest {
            // ----- Arrange -----
            stateFlow.value = stateFlow.value.copy(selectedProgressSheetTab = ProgressSheetTab.PAGE)
            dependencies = stubDependencies(this)
            val action = OnProgressTabClickAction(tab = ProgressSheetTab.PERCENTAGE)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.selectedProgressSheetTab shouldBe ProgressSheetTab.PERCENTAGE
        }

        @Test
        fun `does not mutate any other state field when updating the selected tab`() = runTest {
            // ----- Arrange -----
            val initialState = stateFlow.value
            dependencies = stubDependencies(this)
            val action = OnProgressTabClickAction(tab = ProgressSheetTab.PERCENTAGE)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value shouldBe initialState.copy(selectedProgressSheetTab = ProgressSheetTab.PERCENTAGE)
        }

        @Test
        fun `invokes setLastUsedProgressUnitUseCase with PAGE when tab is PAGE`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)
            val action = OnProgressTabClickAction(tab = ProgressSheetTab.PAGE)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                dependencies.setLastUsedProgressUnitUseCase(unit = ProgressUnit.PAGE)
            }
        }

        @Test
        fun `invokes setLastUsedProgressUnitUseCase with TIME when tab is TIME`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)
            val action = OnProgressTabClickAction(tab = ProgressSheetTab.TIME)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                dependencies.setLastUsedProgressUnitUseCase(unit = ProgressUnit.TIME)
            }
        }

        @Test
        fun `invokes setLastUsedProgressUnitUseCase with PERCENTAGE when tab is PERCENTAGE`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)
            val action = OnProgressTabClickAction(tab = ProgressSheetTab.PERCENTAGE)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                dependencies.setLastUsedProgressUnitUseCase(unit = ProgressUnit.PERCENTAGE)
            }
        }
    }
}
