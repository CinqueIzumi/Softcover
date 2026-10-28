package nl.rhaydus.softcover.feature.profile.presentation.collector

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.domain.model.StatsScope
import nl.rhaydus.softcover.core.domain.model.StatsWindow
import nl.rhaydus.softcover.core.domain.util.ObserveCurrentDateUseCase
import nl.rhaydus.softcover.core.profile.domain.model.ReadingActivity
import nl.rhaydus.softcover.core.profile.domain.model.ReadingStreak
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveReadingActivityUseCase
import nl.rhaydus.softcover.feature.profile.presentation.event.StatsAtlasEvent
import nl.rhaydus.softcover.feature.profile.presentation.mapper.atlasScopeChips
import nl.rhaydus.softcover.feature.profile.presentation.mapper.statsScopeKey
import nl.rhaydus.softcover.feature.profile.presentation.mapper.yearAtAGlanceHeader
import nl.rhaydus.softcover.feature.profile.presentation.mapper.yearGrid
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.StatsAtlasDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.AtlasStreaks
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasLocalVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class YearGridCollectorTest {
    private lateinit var dependencies: StatsAtlasDependencies
    private lateinit var observeReadingActivityUseCase: ObserveReadingActivityUseCase
    private lateinit var stateFlow: MutableStateFlow<StatsAtlasUiState>
    private lateinit var scope: ActionScope<StatsAtlasUiState, StatsAtlasEvent, StatsAtlasLocalVariables>
    private lateinit var dateFlow: MutableStateFlow<LocalDate>

    private val today = LocalDate(
        year = 2026,
        month = 10,
        day = 23,
    )

    private fun date(
        year: Int,
        month: Int,
        day: Int,
    ): LocalDate = LocalDate(
        year = year,
        month = month,
        day = day,
    )

    private fun emptyActivity(): ReadingActivity = ReadingActivity(
        pagesByDay = emptyMap(),
        streaks = emptyList(),
    )

    @BeforeEach
    fun setUp() {
        observeReadingActivityUseCase = mockk()
        dependencies = mockk(relaxed = true)
        dateFlow = MutableStateFlow(today)
        stateFlow = MutableStateFlow(StatsAtlasUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(StatsAtlasLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        val observeCurrentDateUseCase = mockk<ObserveCurrentDateUseCase>()

        every {
            observeCurrentDateUseCase()
        } returns dateFlow

        every {
            dependencies.observeCurrentDateUseCase
        } returns observeCurrentDateUseCase

        every {
            dependencies.observeReadingActivityUseCase
        } returns observeReadingActivityUseCase

        every {
            observeReadingActivityUseCase()
        } returns flowOf(emptyActivity())
    }

    private fun stateFor(selected: StatsScope): StatsAtlasUiState {
        val key = statsScopeKey(selected)

        return StatsAtlasUiState(
            selectedScopeKey = key,
            scopeChips = atlasScopeChips(
                yearsRead = listOf(2026, 2025, 2024),
                selectedKey = key,
            ),
        )
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `writes the grid and header of the selected calendar year`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = stateFor(StatsScope.CalendarYear(2025))
            val activity = ReadingActivity(
                pagesByDay = mapOf(
                    date(
                        year = 2025,
                        month = 4,
                        day = 14,
                    ) to 62,
                ),
                streaks = emptyList(),
            )

            every {
                observeReadingActivityUseCase()
            } returns flowOf(activity)

            // ----- Act -----
            val job = launch {
                YearGridCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            stateFlow.value.yearGrid shouldBe yearGrid(
                activity = activity,
                year = 2025,
            )
            stateFlow.value.yearAtAGlanceHeader shouldBe yearAtAGlanceHeader(year = 2025)
            job.cancel()
        }

        @Test
        fun `shows the year of today for a rolling window`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = stateFor(StatsScope.Rolling(StatsWindow.LAST_12_MONTHS))

            // ----- Act -----
            val job = launch {
                YearGridCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            stateFlow.value.yearGrid.year shouldBe 2026
            stateFlow.value.yearAtAGlanceHeader shouldBe yearAtAGlanceHeader(year = 2026)
            job.cancel()
        }

        @Test
        fun `does not observe the activity again when the scope changes within the same year`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                stateFlow.value = stateFor(StatsScope.AllTime)
                val job = launch {
                    YearGridCollector().onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Act -----
                stateFlow.value = stateFor(StatsScope.CalendarYear(2026))

                // ----- Assert -----
                verify(exactly = 1) {
                    observeReadingActivityUseCase()
                }
                job.cancel()
            }

        @Test
        fun `re-keys the grid when the scope moves to another year`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = stateFor(StatsScope.CalendarYear(2025))
            val job = launch {
                YearGridCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFor(StatsScope.CalendarYear(2024))

            // ----- Assert -----
            stateFlow.value.yearGrid.year shouldBe 2024
            stateFlow.value.yearAtAGlanceHeader shouldBe yearAtAGlanceHeader(year = 2024)
            job.cancel()
        }
    }

    @Nested
    inner class StreakTiles {
        private val longestOf2025 = ReadingStreak(
            start = date(
                year = 2025,
                month = 4,
                day = 10,
            ),
            end = date(
                year = 2025,
                month = 4,
                day = 14,
            ),
        )

        private val runningNow = ReadingStreak(
            start = date(
                year = 2026,
                month = 10,
                day = 20,
            ),
            end = date(
                year = 2026,
                month = 10,
                day = 22,
            ),
        )

        private val activity = ReadingActivity(
            pagesByDay = emptyMap(),
            streaks = listOf(longestOf2025, runningNow),
        )

        @Test
        fun `keys the tiles and the day sheet streaks to the atlas year and today`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                stateFlow.value = stateFor(StatsScope.CalendarYear(2025))

                every {
                    observeReadingActivityUseCase()
                } returns flowOf(activity)

                // ----- Act -----
                val job = launch {
                    YearGridCollector().onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Assert -----
                stateFlow.value.longestStreakTile.stat.value shouldBe 5.0
                stateFlow.value.currentStreakTile.stat.value shouldBe 3.0
                stateFlow.value.atlasStreaks shouldBe AtlasStreaks(
                    activity = activity,
                    longestInAtlasYear = longestOf2025,
                    current = runningNow,
                )
                job.cancel()
            }

        @Test
        fun `lets the current streak lapse when the date moves on`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = stateFor(StatsScope.CalendarYear(2025))

            every {
                observeReadingActivityUseCase()
            } returns flowOf(activity)

            val job = launch {
                YearGridCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            dateFlow.value = date(
                year = 2026,
                month = 10,
                day = 24,
            )

            // ----- Assert -----
            stateFlow.value.currentStreakTile.stat.value shouldBe 0.0
            stateFlow.value.atlasStreaks.current.shouldBeNull()
            job.cancel()
        }

        @Test
        fun `moves the grid and the longest tile to the new year when the date rolls over`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                dateFlow.value = date(
                    year = 2026,
                    month = 12,
                    day = 31,
                )
                stateFlow.value = stateFor(StatsScope.AllTime)

                every {
                    observeReadingActivityUseCase()
                } returns flowOf(activity)

                val job = launch {
                    YearGridCollector().onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Act -----
                dateFlow.value = date(
                    year = 2027,
                    month = 1,
                    day = 1,
                )

                // ----- Assert -----
                stateFlow.value.yearGrid.year shouldBe 2027
                stateFlow.value.longestStreakTile.stat.value shouldBe 0.0
                job.cancel()
            }

        @Test
        fun `follows new activity without re-keying the year`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val emissions = MutableStateFlow(emptyActivity())

            every {
                observeReadingActivityUseCase()
            } returns emissions

            stateFlow.value = stateFor(StatsScope.CalendarYear(2025))
            val job = launch {
                YearGridCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            emissions.value = activity

            // ----- Assert -----
            stateFlow.value.longestStreakTile.stat.value shouldBe 5.0
            job.cancel()
        }
    }
}
