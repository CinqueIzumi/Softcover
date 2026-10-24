package nl.rhaydus.softcover.feature.profile.presentation.collector

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.statistic.StatTileVariant
import nl.rhaydus.softcover.core.domain.model.StatsRange
import nl.rhaydus.softcover.core.domain.model.StatsScope
import nl.rhaydus.softcover.core.domain.model.StatsWindow
import nl.rhaydus.softcover.core.domain.util.ObserveCurrentDateUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetHideUntaggedAuthorsAsFlowUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.ObserveAuthorDemographicsBackfilledUseCase
import nl.rhaydus.softcover.core.profile.domain.model.AuthorDemographics
import nl.rhaydus.softcover.core.profile.domain.model.DemographicBreakdown
import nl.rhaydus.softcover.core.profile.domain.model.GenreBreakdown
import nl.rhaydus.softcover.core.profile.domain.model.GenreSlice
import nl.rhaydus.softcover.core.profile.domain.model.RatingBand
import nl.rhaydus.softcover.core.profile.domain.model.RatingsDistribution
import nl.rhaydus.softcover.core.profile.domain.model.ReadingHistory
import nl.rhaydus.softcover.core.profile.domain.model.ReadingStats
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveReadingStatsUseCase
import nl.rhaydus.softcover.feature.profile.presentation.event.StatsAtlasEvent
import nl.rhaydus.softcover.feature.profile.presentation.mapper.atlasScopeChips
import nl.rhaydus.softcover.feature.profile.presentation.mapper.demographicBreakdownUiModel
import nl.rhaydus.softcover.feature.profile.presentation.mapper.statsScopeKey
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.StatsAtlasDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasLocalVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class AtlasScopedSectionsCollectorTest {
    private lateinit var observeReadingStatsUseCase: ObserveReadingStatsUseCase
    private lateinit var observeAuthorDemographicsBackfilledUseCase: ObserveAuthorDemographicsBackfilledUseCase
    private lateinit var getHideUntaggedAuthorsAsFlowUseCase: GetHideUntaggedAuthorsAsFlowUseCase
    private lateinit var backfilledFlow: MutableStateFlow<Boolean>
    private lateinit var hideFlow: MutableStateFlow<Boolean>
    private lateinit var stateFlow: MutableStateFlow<StatsAtlasUiState>
    private lateinit var scope: ActionScope<StatsAtlasUiState, StatsAtlasEvent, StatsAtlasLocalVariables>
    private val observedRanges: MutableList<StatsRange> = mutableListOf()
    private var statsFor: (StatsRange) -> ReadingStats = { ReadingStats() }

    @BeforeEach
    fun setUp() {
        backfilledFlow = MutableStateFlow(true)
        hideFlow = MutableStateFlow(false)
        observedRanges.clear()
        statsFor = { ReadingStats() }
        observeReadingStatsUseCase = mockk()
        observeAuthorDemographicsBackfilledUseCase = mockk()
        getHideUntaggedAuthorsAsFlowUseCase = mockk()
        stateFlow = MutableStateFlow(stateFor(StatsScope.AllTime))
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(StatsAtlasLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        every {
            observeReadingStatsUseCase(any())
        } answers {
            val range = firstArg<StatsRange>()
            observedRanges += range
            flowOf(statsFor(range))
        }

        every {
            observeAuthorDemographicsBackfilledUseCase()
        } returns backfilledFlow

        every {
            getHideUntaggedAuthorsAsFlowUseCase()
        } returns hideFlow
    }

    private fun stateFor(
        selected: StatsScope,
        readingHistory: ReadingHistory? = ReadingHistory(),
    ): StatsAtlasUiState {
        val key = statsScopeKey(selected)

        return StatsAtlasUiState(
            readingHistory = readingHistory,
            selectedScopeKey = key,
            scopeChips = atlasScopeChips(
                yearsRead = listOf(2026, 2025),
                selectedKey = key,
            ),
        )
    }

    private fun TestScope.launchCollector(
        dateFlow: Flow<LocalDate> = flowOf(
            LocalDate(
                2026,
                10,
                23,
            ),
        ),
    ): Job {
        val observeCurrentDateUseCase = mockk<ObserveCurrentDateUseCase>()
        val dependencies = mockk<StatsAtlasDependencies>(relaxed = true)

        every {
            observeCurrentDateUseCase()
        } returns dateFlow

        every {
            dependencies.observeCurrentDateUseCase
        } returns observeCurrentDateUseCase

        every {
            dependencies.observeReadingStatsUseCase
        } returns observeReadingStatsUseCase

        every {
            dependencies.observeAuthorDemographicsBackfilledUseCase
        } returns observeAuthorDemographicsBackfilledUseCase

        every {
            dependencies.getHideUntaggedAuthorsAsFlowUseCase
        } returns getHideUntaggedAuthorsAsFlowUseCase

        return launch {
            AtlasScopedSectionsCollector().onLaunch(
                scope = scope,
                dependencies = dependencies,
            )
        }
    }

    private fun StatsAtlasUiState.heroCaption(): String = (opening.hero.variant as StatTileVariant.Hero).caption

    @Nested
    inner class OnLaunch {
        @Test
        fun `queries the stats for the range the selected scope resolves to`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = stateFor(StatsScope.CalendarYear(2025))

            // ----- Act -----
            val job = launchCollector()

            // ----- Assert -----
            observedRanges.shouldContainExactly(
                StatsRange.Bounded(
                    start = LocalDate(
                        year = 2025,
                        month = 1,
                        day = 1,
                    ),
                    endInclusive = LocalDate(
                        year = 2025,
                        month = 12,
                        day = 31,
                    ),
                ),
            )
            job.cancel()
        }

        @Test
        fun `queries a rolling window ending on the clock day`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = stateFor(StatsScope.Rolling(StatsWindow.LAST_30_DAYS))

            // ----- Act -----
            val job = launchCollector()

            // ----- Assert -----
            observedRanges.shouldContainExactly(
                StatsRange.Bounded(
                    start = LocalDate(
                        year = 2026,
                        month = 9,
                        day = 24,
                    ),
                    endInclusive = LocalDate(
                        year = 2026,
                        month = 10,
                        day = 23,
                    ),
                ),
            )
            job.cancel()
        }

        @Test
        fun `re-resolves a rolling window when the date flow emits a new day`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val dateFlow = MutableStateFlow(
                LocalDate(
                    year = 2026,
                    month = 10,
                    day = 23,
                ),
            )
            stateFlow.value = stateFor(StatsScope.Rolling(StatsWindow.LAST_30_DAYS))
            val job = launchCollector(dateFlow = dateFlow)

            // ----- Act -----
            dateFlow.value = LocalDate(
                year = 2026,
                month = 10,
                day = 24,
            )

            // ----- Assert -----
            observedRanges.last() shouldBe StatsRange.Bounded(
                start = LocalDate(
                    year = 2026,
                    month = 9,
                    day = 25,
                ),
                endInclusive = LocalDate(
                    year = 2026,
                    month = 10,
                    day = 24,
                ),
            )
            job.cancel()
        }

        @Test
        fun `re-queries with the new range when the scope changes`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val job = launchCollector()

            // ----- Act -----
            stateFlow.value = stateFor(StatsScope.CalendarYear(2025))

            // ----- Assert -----
            observedRanges.first() shouldBe StatsRange.Unbounded
            observedRanges.size shouldBe 2
            observedRanges.last() shouldNotBe StatsRange.Unbounded
            stateFlow.value.scopeLine shouldBe "Showing 2025"
            job.cancel()
        }

        @Test
        fun `does not query again when an unrelated part of the state changes`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val job = launchCollector()

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(isLoading = false)

            // ----- Assert -----
            observedRanges.shouldContainExactly(StatsRange.Unbounded)
            job.cancel()
        }

        @Test
        fun `stays idle until the reading history has landed`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = stateFor(
                selected = StatsScope.AllTime,
                readingHistory = null,
            )

            // ----- Act -----
            val job = launchCollector()

            // ----- Assert -----
            observedRanges.shouldBeEmpty()
            stateFlow.value.readingStats.shouldBeNull()
            job.cancel()
        }

        @Test
        fun `starts querying once the reading history lands`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = stateFor(
                selected = StatsScope.AllTime,
                readingHistory = null,
            )
            val job = launchCollector()

            // ----- Act -----
            stateFlow.value = stateFor(StatsScope.AllTime)

            // ----- Assert -----
            observedRanges.shouldContainExactly(StatsRange.Unbounded)
            job.cancel()
        }

        @Test
        fun `keeps the sections loading while the library is not backfilled`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            backfilledFlow.value = false

            // ----- Act -----
            val job = launchCollector()

            // ----- Assert -----
            stateFlow.value.readingStats.shouldBeNull()
            stateFlow.value.opening.hero.stat.isLoading shouldBe true
            job.cancel()
        }

        @Test
        fun `fills the sections once the backfill flag turns true`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            backfilledFlow.value = false
            statsFor = { ReadingStats(booksRead = 6) }
            val job = launchCollector()

            // ----- Act -----
            backfilledFlow.value = true

            // ----- Assert -----
            stateFlow.value.opening.books.stat.value shouldBe 6.0
            stateFlow.value.opening.books.stat.isLoading shouldBe false
            job.cancel()
        }

        @Test
        fun `writes the stats of the scope into the opening tiles and genres`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = stateFor(StatsScope.CalendarYear(2025))
            statsFor = {
                ReadingStats(
                    booksRead = 5,
                    totalPages = 1_500,
                    genres = GenreBreakdown(
                        slices = listOf(
                            GenreSlice(
                                name = "Fantasy",
                                count = 3,
                                fraction = 0.6,
                            ),
                        ),
                        taggedBookCount = 5,
                    ),
                )
            }

            // ----- Act -----
            val job = launchCollector()

            // ----- Assert -----
            stateFlow.value.opening.hero.stat.value shouldBe 1_500.0
            stateFlow.value.heroCaption() shouldBe "From 5 finished books in 2025."
            stateFlow.value.hasGenres shouldBe true
            stateFlow.value.genreRanking.rows.map { it.label } shouldBe listOf("Fantasy")
            job.cancel()
        }

        @Test
        fun `an empty scope yields the empty framing sentences`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = stateFor(StatsScope.CalendarYear(2025))
            statsFor = { ReadingStats() }

            // ----- Act -----
            val job = launchCollector()

            // ----- Assert -----
            stateFlow.value.hasGenres shouldBe false
            stateFlow.value.genreEmptyCaption shouldBe "No finished books in 2025."
            stateFlow.value.ratingsEmptyCaption shouldBe "No finished books in 2025."
            stateFlow.value.authorsEmptyCaption shouldBe "No finished books in 2025."
            stateFlow.value.heroCaption() shouldBe "No books finished in 2025."
            job.cancel()
        }

        @Test
        fun `a narrow scope with few ratings states the count`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = stateFor(StatsScope.Rolling(StatsWindow.LAST_30_DAYS))
            statsFor = {
                ReadingStats(
                    booksRead = 3,
                    ratings = RatingsDistribution(
                        totalRatings = 3,
                        band = RatingBand.NEW_READER,
                    ),
                )
            }

            // ----- Act -----
            val job = launchCollector()

            // ----- Assert -----
            val header = stateFlow.value.ratingsHistogramHeader as SectionHeaderUiModel.Section

            header.headlineText shouldBe "3 rated books in the last 30 days"
            job.cancel()
        }

        @Test
        fun `all time keeps the band copy for few ratings`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            statsFor = {
                ReadingStats(
                    booksRead = 3,
                    ratings = RatingsDistribution(
                        totalRatings = 3,
                        band = RatingBand.NEW_READER,
                    ),
                )
            }

            // ----- Act -----
            val job = launchCollector()

            // ----- Assert -----
            val header = stateFlow.value.ratingsHistogramHeader as SectionHeaderUiModel.Section

            header.headlineText shouldBe "Too early to tell"
            job.cancel()
        }

        @Test
        fun `hide untagged authors drops the unknown authors from the scoped demographics`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                hideFlow.value = true
                statsFor = {
                    ReadingStats(
                        booksRead = 4,
                        authorDemographics = AuthorDemographics(
                            bipocBreakdown = DemographicBreakdown(
                                yesCount = 2,
                                noCount = 1,
                                unknownCount = 5,
                            ),
                        ),
                    )
                }

                // ----- Act -----
                val job = launchCollector()

                // ----- Assert -----
                stateFlow.value.bipocBreakdown shouldBe demographicBreakdownUiModel(
                    breakdown = DemographicBreakdown(
                        yesCount = 2,
                        noCount = 1,
                        unknownCount = 0,
                    ),
                    hideUntaggedAuthors = true,
                )
                job.cancel()
            }

        @Test
        fun `keeps the unknown authors while hide untagged authors is off`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val breakdown = DemographicBreakdown(
                yesCount = 2,
                noCount = 1,
                unknownCount = 5,
            )
            statsFor = {
                ReadingStats(
                    booksRead = 4,
                    authorDemographics = AuthorDemographics(bipocBreakdown = breakdown),
                )
            }

            // ----- Act -----
            val job = launchCollector()

            // ----- Assert -----
            stateFlow.value.bipocBreakdown shouldBe demographicBreakdownUiModel(
                breakdown = breakdown,
                hideUntaggedAuthors = false,
            )
            job.cancel()
        }
    }
}
