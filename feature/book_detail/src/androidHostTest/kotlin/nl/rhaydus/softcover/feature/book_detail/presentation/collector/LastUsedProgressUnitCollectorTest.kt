package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.designsystem.presentation.model.ProgressSheetTab
import nl.rhaydus.softcover.core.domain.model.ProgressUnit
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetLastUsedProgressUnitAsFlowUseCase
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class LastUsedProgressUnitCollectorTest {
    private lateinit var getLastUsedProgressUnitAsFlowUseCase: GetLastUsedProgressUnitAsFlowUseCase
    private lateinit var dependencies: BookDetailDependencies
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>
    private lateinit var progressUnitFlow: MutableSharedFlow<ProgressUnit>

    @BeforeEach
    fun setUp() {
        progressUnitFlow = MutableSharedFlow()
        getLastUsedProgressUnitAsFlowUseCase = mockk()
        stateFlow = MutableStateFlow(BookDetailUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(BookDetailLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        every {
            getLastUsedProgressUnitAsFlowUseCase()
        } returns progressUnitFlow

        dependencies = mockk<BookDetailDependencies>(relaxed = true).also { mock ->
            every {
                mock.getLastUsedProgressUnitAsFlowUseCase
            } returns getLastUsedProgressUnitAsFlowUseCase
        }
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `updates selectedProgressSheetTab when flow emits ProgressUnit TIME`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = LastUsedProgressUnitCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            progressUnitFlow.emit(ProgressUnit.TIME)

            // ----- Assert -----
            stateFlow.value.selectedProgressSheetTab shouldBe ProgressSheetTab.TIME
            job.cancel()
        }

        @Test
        fun `updates selectedProgressSheetTab when flow emits ProgressUnit PAGE`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = LastUsedProgressUnitCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            progressUnitFlow.emit(ProgressUnit.PAGE)

            // ----- Assert -----
            stateFlow.value.selectedProgressSheetTab shouldBe ProgressSheetTab.PAGE
            job.cancel()
        }

        @Test
        fun `updates selectedProgressSheetTab when flow emits ProgressUnit PERCENTAGE`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = LastUsedProgressUnitCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            progressUnitFlow.emit(ProgressUnit.PERCENTAGE)

            // ----- Assert -----
            stateFlow.value.selectedProgressSheetTab shouldBe ProgressSheetTab.PERCENTAGE
            job.cancel()
        }

        @Test
        fun `updates selectedProgressSheetTab to the latest value when flow emits multiple times`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = LastUsedProgressUnitCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            progressUnitFlow.emit(ProgressUnit.TIME)
            progressUnitFlow.emit(ProgressUnit.PAGE)

            // ----- Assert -----
            stateFlow.value.selectedProgressSheetTab shouldBe ProgressSheetTab.PAGE
            job.cancel()
        }

        @Test
        fun `does not change selectedProgressSheetTab before the flow emits`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = LastUsedProgressUnitCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act & Assert -----
            stateFlow.value.selectedProgressSheetTab shouldBe BookDetailUiState().selectedProgressSheetTab
            job.cancel()
        }

        @Test
        fun `retains the last emitted selectedProgressSheetTab after the collector job is cancelled`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = LastUsedProgressUnitCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }
            progressUnitFlow.emit(ProgressUnit.TIME)
            job.cancel()

            // ----- Act & Assert -----
            stateFlow.value.selectedProgressSheetTab shouldBe ProgressSheetTab.TIME
        }

        @Test
        fun `preserves all other state fields when updating selectedProgressSheetTab`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = BookDetailUiState(
                loadingBookDetails = false,
                fabMenuExpanded = true,
            )
            val collector = LastUsedProgressUnitCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            progressUnitFlow.emit(ProgressUnit.TIME)

            // ----- Assert -----
            stateFlow.value.loadingBookDetails shouldBe false
            stateFlow.value.fabMenuExpanded shouldBe true
            stateFlow.value.selectedProgressSheetTab shouldBe ProgressSheetTab.TIME
            job.cancel()
        }
    }
}
