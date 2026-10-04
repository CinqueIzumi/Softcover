package nl.rhaydus.softcover.feature.explore.presentation.collector

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.bookcard.BookCardKey
import nl.rhaydus.softcover.feature.explore.domain.model.DismissedSeries
import nl.rhaydus.softcover.feature.explore.domain.usecase.GetDismissedContinueSeriesUseCase
import nl.rhaydus.softcover.feature.explore.presentation.action.OnUnblockSeriesAction
import nl.rhaydus.softcover.feature.explore.presentation.event.HiddenSuggestionsEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.HiddenSuggestionsDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.HiddenSuggestionsLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.HiddenSuggestionsUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class DismissedSeriesCollectorTest {
    private lateinit var getDismissedContinueSeriesUseCase: GetDismissedContinueSeriesUseCase
    private lateinit var dependencies: HiddenSuggestionsDependencies
    private lateinit var stateFlow: MutableStateFlow<HiddenSuggestionsUiState>
    private lateinit var scope: ActionScope<HiddenSuggestionsUiState, HiddenSuggestionsEvent, HiddenSuggestionsLocalVariables>
    private lateinit var dismissedSeriesFlow: MutableSharedFlow<List<DismissedSeries>>

    @BeforeEach
    fun setUp() {
        dismissedSeriesFlow = MutableSharedFlow()
        getDismissedContinueSeriesUseCase = mockk()
        stateFlow = MutableStateFlow(HiddenSuggestionsUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(HiddenSuggestionsLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        every {
            getDismissedContinueSeriesUseCase()
        } returns dismissedSeriesFlow

        dependencies = mockk<HiddenSuggestionsDependencies>(relaxed = true).also { mock ->
            every {
                mock.getDismissedContinueSeriesUseCase
            } returns getDismissedContinueSeriesUseCase
        }
    }

    private fun stubDismissedSeries(
        seriesId: Int,
        seriesName: String? = "Series $seriesId",
    ): DismissedSeries = DismissedSeries(
        seriesId = seriesId,
        seriesName = seriesName,
        coverUrl = null,
        authorText = null,
        bookCount = null,
    )

    // ----- OnLaunch -----

    @Nested
    inner class OnLaunch {
        @Test
        fun `maps each emitted series to a card whose payload restores that series`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val series = stubDismissedSeries(seriesId = 1)
            val collector = DismissedSeriesCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            dismissedSeriesFlow.emit(listOf(series))

            // ----- Assert -----
            val hidden = stateFlow.value.hiddenSeries
            hidden.cards.map { it.key } shouldBe listOf(BookCardKey.Series(seriesId = 1))
            val action = hidden[BookCardKey.Series(seriesId = 1)].shouldBeInstanceOf<OnUnblockSeriesAction>()
            action.seriesId shouldBe 1
            action.seriesName shouldBe series.seriesName
            job.cancel()
        }

        @Test
        fun `leaves the hiddenBooks stand-ins in place when only series are emitted`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = DismissedSeriesCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            dismissedSeriesFlow.emit(emptyList())

            // ----- Assert -----
            stateFlow.value.isLoadingSeries shouldBe false
            stateFlow.value.isLoadingBooks shouldBe true
            job.cancel()
        }

        @Test
        fun `emits an empty card list rather than stand-ins when the emitted list is empty`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val collector = DismissedSeriesCollector()
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Act -----
                dismissedSeriesFlow.emit(emptyList())

                // ----- Assert -----
                stateFlow.value.hiddenSeries.cards.shouldBeEmpty()
                job.cancel()
            }

        @Test
        fun `sets the group header count to the emitted list size`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val series = listOf(
                stubDismissedSeries(seriesId = 1),
                stubDismissedSeries(seriesId = 2),
                stubDismissedSeries(seriesId = 3),
            )
            val collector = DismissedSeriesCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            dismissedSeriesFlow.emit(series)

            // ----- Assert -----
            stateFlow.value.hiddenSeriesGroupHeader.count shouldBe "3"
            job.cancel()
        }

        @Test
        fun `sets the group header count to zero for an empty emission`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = DismissedSeriesCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            dismissedSeriesFlow.emit(emptyList())

            // ----- Assert -----
            stateFlow.value.hiddenSeriesGroupHeader.count shouldBe "0"
            job.cancel()
        }

        @Test
        fun `updates the count again on a later emission without touching the header text`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = DismissedSeriesCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }
            dismissedSeriesFlow.emit(listOf(stubDismissedSeries(seriesId = 1), stubDismissedSeries(seriesId = 2)))
            stateFlow.value.hiddenSeriesGroupHeader.count shouldBe "2"

            // ----- Act -----
            dismissedSeriesFlow.emit(listOf(stubDismissedSeries(seriesId = 1)))

            // ----- Assert -----
            stateFlow.value.hiddenSeriesGroupHeader.count shouldBe "1"
            stateFlow.value.hiddenSeriesGroupHeader.header shouldBe HiddenSuggestionsUiState().hiddenSeriesGroupHeader.header
            stateFlow.value.hiddenSeriesGroupHeader.subline shouldBe HiddenSuggestionsUiState().hiddenSeriesGroupHeader.subline
            job.cancel()
        }

        @Test
        fun `preserves the books group header when updating the series header`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = HiddenSuggestionsUiState(
                hiddenBooksGroupHeader = HiddenSuggestionsUiState().hiddenBooksGroupHeader.copy(count = "9"),
            )
            val collector = DismissedSeriesCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            dismissedSeriesFlow.emit(listOf(stubDismissedSeries(seriesId = 1)))

            // ----- Assert -----
            stateFlow.value.hiddenBooksGroupHeader.count shouldBe "9"
            job.cancel()
        }
    }
}
