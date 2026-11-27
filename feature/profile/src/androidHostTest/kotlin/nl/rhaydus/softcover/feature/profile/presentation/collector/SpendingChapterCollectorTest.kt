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
import nl.rhaydus.softcover.core.domain.model.Currency
import nl.rhaydus.softcover.core.domain.model.StatsRange
import nl.rhaydus.softcover.core.domain.util.ObserveCurrentDateUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetCurrenciesUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetUsualCurrencyAsFlowUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.ObserveReadDatesBackfilledUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.ObserveReadingStatsBackfilledUseCase
import nl.rhaydus.softcover.core.profile.domain.model.CurrencySpend
import nl.rhaydus.softcover.core.profile.domain.model.ReadingHistory
import nl.rhaydus.softcover.core.profile.domain.model.SourceSpend
import nl.rhaydus.softcover.core.profile.domain.model.SpendSource
import nl.rhaydus.softcover.core.profile.domain.model.SpendStats
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveSpendStatsUseCase
import nl.rhaydus.softcover.feature.profile.presentation.event.StatsAtlasEvent
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.StatsAtlasDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasLocalVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SpendingChapterCollectorTest {
    private val eur = Currency(
        code = "EUR",
        name = "Euro",
        symbol = "€",
        fractionDigits = 2,
    )

    private lateinit var observeSpendStatsUseCase: ObserveSpendStatsUseCase
    private lateinit var observeReadingStatsBackfilledUseCase: ObserveReadingStatsBackfilledUseCase
    private lateinit var observeReadDatesBackfilledUseCase: ObserveReadDatesBackfilledUseCase
    private lateinit var dependencies: StatsAtlasDependencies
    private lateinit var backfilledFlow: MutableStateFlow<Boolean>
    private lateinit var stateFlow: MutableStateFlow<StatsAtlasUiState>
    private lateinit var scope: ActionScope<StatsAtlasUiState, StatsAtlasEvent, StatsAtlasLocalVariables>
    private val requestedRanges: MutableList<StatsRange> = mutableListOf()

    private val stats = SpendStats(
        countedBooks = 3,
        pricedBooks = 2,
        currencies = listOf(
            CurrencySpend(
                currencyCode = "EUR",
                totalThousandths = 98_000,
                pricedBooks = 2,
                medianThousandths = 49_000,
                sources = listOf(
                    SourceSpend(
                        SpendSource.NotNoted,
                        98_000,
                        2,
                    ),
                ),
            ),
        ),
    )

    @BeforeEach
    fun setUp() {
        requestedRanges.clear()
        backfilledFlow = MutableStateFlow(true)
        stateFlow = MutableStateFlow(StatsAtlasUiState(readingHistory = ReadingHistory()))

        observeSpendStatsUseCase = mockk()
        observeReadingStatsBackfilledUseCase = mockk()
        observeReadDatesBackfilledUseCase = mockk()

        every {
            observeSpendStatsUseCase(any())
        } answers {
            requestedRanges += firstArg<StatsRange>()
            flowOf(stats)
        }

        every {
            observeReadingStatsBackfilledUseCase()
        } returns backfilledFlow

        every {
            observeReadDatesBackfilledUseCase()
        } returns MutableStateFlow(true)

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

        val getCurrenciesUseCase = mockk<GetCurrenciesUseCase>()

        every {
            getCurrenciesUseCase()
        } returns listOf(eur)

        val getUsualCurrencyAsFlowUseCase = mockk<GetUsualCurrencyAsFlowUseCase>()

        every {
            getUsualCurrencyAsFlowUseCase()
        } returns flowOf(eur)

        dependencies = mockk(relaxed = true)

        every {
            dependencies.observeSpendStatsUseCase
        } returns observeSpendStatsUseCase

        every {
            dependencies.observeReadingStatsBackfilledUseCase
        } returns observeReadingStatsBackfilledUseCase

        every {
            dependencies.observeReadDatesBackfilledUseCase
        } returns observeReadDatesBackfilledUseCase

        every {
            dependencies.observeCurrentDateUseCase
        } returns observeCurrentDateUseCase

        every {
            dependencies.getCurrenciesUseCase
        } returns getCurrenciesUseCase

        every {
            dependencies.getUsualCurrencyAsFlowUseCase
        } returns getUsualCurrencyAsFlowUseCase

        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(StatsAtlasLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `fills the chapter from the stats of the selected scope`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val job = launch {
                SpendingChapterCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            val chapter = stateFlow.value.spendingChapter

            // ----- Assert -----
            listOf(chapter.spent?.stat?.value, chapter.spent?.caption, requestedRanges) shouldBe
                listOf(98.0, "All in EUR.", listOf(StatsRange.Unbounded))

            job.cancel()
        }

        @Test
        fun `stays idle until the reading history has landed`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = StatsAtlasUiState(readingHistory = null)

            val job = launch {
                SpendingChapterCollector().onLaunch(
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
        fun `keeps the masked chapter until the library is backfilled`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            backfilledFlow.value = false

            val job = launch {
                SpendingChapterCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            val whileUnfilled = stateFlow.value.spendingChapter.header.isLoading

            // ----- Act -----
            backfilledFlow.value = true

            // ----- Assert -----
            listOf(whileUnfilled, stateFlow.value.spendingChapter.header.isLoading) shouldBe listOf(true, false)
            job.cancel()
        }
    }
}
