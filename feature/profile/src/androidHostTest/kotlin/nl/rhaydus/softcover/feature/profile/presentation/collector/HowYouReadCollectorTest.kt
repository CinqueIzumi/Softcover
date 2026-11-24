package nl.rhaydus.softcover.feature.profile.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.domain.model.Pace
import nl.rhaydus.softcover.core.domain.model.StatsRange
import nl.rhaydus.softcover.core.domain.model.StatsScope
import nl.rhaydus.softcover.core.domain.util.ObserveCurrentDateUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.ObserveReadDatesBackfilledUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.ObserveReadingStatsBackfilledUseCase
import nl.rhaydus.softcover.core.profile.domain.model.HowYouReadStats
import nl.rhaydus.softcover.core.profile.domain.model.MoodProfile
import nl.rhaydus.softcover.core.profile.domain.model.PlotCharacterSplit
import nl.rhaydus.softcover.core.profile.domain.model.ReadingHistory
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveHowYouReadStatsUseCase
import nl.rhaydus.softcover.feature.profile.presentation.event.StatsAtlasEvent
import nl.rhaydus.softcover.feature.profile.presentation.mapper.atlasScopeChips
import nl.rhaydus.softcover.feature.profile.presentation.mapper.statsScopeKey
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.StatsAtlasDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasLocalVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class HowYouReadCollectorTest {
    private lateinit var dependencies: StatsAtlasDependencies
    private lateinit var statsBackfilledFlow: MutableStateFlow<Boolean>
    private lateinit var readDatesBackfilledFlow: MutableStateFlow<Boolean>
    private lateinit var stateFlow: MutableStateFlow<StatsAtlasUiState>
    private lateinit var scope: ActionScope<StatsAtlasUiState, StatsAtlasEvent, StatsAtlasLocalVariables>
    private val requestedRanges: MutableList<StatsRange> = mutableListOf()

    private val stats = HowYouReadStats(
        plotCharacter = PlotCharacterSplit(
            plot = 3,
            mix = 0,
            character = 1,
        ),
        paceCounts = mapOf(Pace.FAST to 1),
        writingStyleCounts = emptyMap(),
        writingFlagCounts = emptyMap(),
        booksWithPace = 1,
        booksWithWritingStyle = 0,
        booksWithWritingFlag = 0,
        moods = MoodProfile(
            booksWithMoods = 0,
            topMoods = emptyList(),
        ),
    )

    @BeforeEach
    fun setUp() {
        requestedRanges.clear()
        statsBackfilledFlow = MutableStateFlow(true)
        readDatesBackfilledFlow = MutableStateFlow(true)
        stateFlow = MutableStateFlow(StatsAtlasUiState(readingHistory = ReadingHistory()))

        val observeHowYouReadStatsUseCase = mockk<ObserveHowYouReadStatsUseCase>()

        every {
            observeHowYouReadStatsUseCase(any())
        } answers {
            requestedRanges += firstArg<StatsRange>()
            flowOf(stats)
        }

        val observeReadingStatsBackfilledUseCase = mockk<ObserveReadingStatsBackfilledUseCase>()

        every {
            observeReadingStatsBackfilledUseCase()
        } returns statsBackfilledFlow

        val observeReadDatesBackfilledUseCase = mockk<ObserveReadDatesBackfilledUseCase>()

        every {
            observeReadDatesBackfilledUseCase()
        } returns readDatesBackfilledFlow

        val observeCurrentDateUseCase = mockk<ObserveCurrentDateUseCase>()

        every {
            observeCurrentDateUseCase()
        } returns flowOf(
            LocalDate(
                2026,
                10,
                23,
            ),
        )

        dependencies = mockk(relaxed = true)

        every {
            dependencies.observeHowYouReadStatsUseCase
        } returns observeHowYouReadStatsUseCase

        every {
            dependencies.observeReadingStatsBackfilledUseCase
        } returns observeReadingStatsBackfilledUseCase

        every {
            dependencies.observeReadDatesBackfilledUseCase
        } returns observeReadDatesBackfilledUseCase

        every {
            dependencies.observeCurrentDateUseCase
        } returns observeCurrentDateUseCase

        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(StatsAtlasLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `fills the block from the stats of the selected scope`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val job = launch {
                HowYouReadCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            val howYouRead = stateFlow.value.howYouRead

            // ----- Assert -----
            listOf(howYouRead.isLoading, howYouRead.split?.title, requestedRanges) shouldBe
                listOf(false, "Plot first", listOf(StatsRange.Unbounded))

            job.cancel()
        }

        @Test
        fun `stays idle until the reading history has landed`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = StatsAtlasUiState(readingHistory = null)

            val job = launch {
                HowYouReadCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            val before = requestedRanges.size

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(readingHistory = ReadingHistory())

            // ----- Assert -----
            listOf(before, requestedRanges.size) shouldBe listOf(0, 1)
            job.cancel()
        }

        @Test
        fun `keeps the masked block until both backfills are done`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            readDatesBackfilledFlow.value = false

            val job = launch {
                HowYouReadCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            val whileUnfilled = stateFlow.value.howYouRead.isLoading

            // ----- Act -----
            readDatesBackfilledFlow.value = true

            // ----- Assert -----
            listOf(whileUnfilled, stateFlow.value.howYouRead.isLoading) shouldBe listOf(true, false)
            job.cancel()
        }

        @Test
        fun `observes the stats again when the scope changes`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val job = launch {
                HowYouReadCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            val before = requestedRanges.size
            val key = statsScopeKey(StatsScope.CalendarYear(2025))

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(
                selectedScopeKey = key,
                scopeChips = atlasScopeChips(
                    yearsRead = listOf(2025),
                    selectedKey = key,
                ),
            )

            // ----- Assert -----
            listOf(before, requestedRanges.size) shouldBe listOf(1, 2)
            job.cancel()
        }
    }
}
