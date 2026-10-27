package nl.rhaydus.softcover.feature.profile.presentation.collector

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
import nl.rhaydus.softcover.core.profile.domain.model.DailyPages
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveDailyPagesReadUseCase
import nl.rhaydus.softcover.feature.profile.presentation.event.StatsAtlasEvent
import nl.rhaydus.softcover.feature.profile.presentation.mapper.atlasScopeChips
import nl.rhaydus.softcover.feature.profile.presentation.mapper.statsScopeKey
import nl.rhaydus.softcover.feature.profile.presentation.mapper.yearAtAGlanceHeader
import nl.rhaydus.softcover.feature.profile.presentation.mapper.yearGrid
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.StatsAtlasDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasLocalVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class YearGridCollectorTest {
    private lateinit var dependencies: StatsAtlasDependencies
    private lateinit var observeDailyPagesReadUseCase: ObserveDailyPagesReadUseCase
    private lateinit var stateFlow: MutableStateFlow<StatsAtlasUiState>
    private lateinit var scope: ActionScope<StatsAtlasUiState, StatsAtlasEvent, StatsAtlasLocalVariables>

    private val today = LocalDate(
        year = 2026,
        month = 10,
        day = 23,
    )

    @BeforeEach
    fun setUp() {
        observeDailyPagesReadUseCase = mockk()
        dependencies = mockk(relaxed = true)
        stateFlow = MutableStateFlow(StatsAtlasUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(StatsAtlasLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        val observeCurrentDateUseCase = mockk<ObserveCurrentDateUseCase>()

        every {
            observeCurrentDateUseCase()
        } returns flowOf(today)

        every {
            dependencies.observeCurrentDateUseCase
        } returns observeCurrentDateUseCase

        every {
            dependencies.observeDailyPagesReadUseCase
        } returns observeDailyPagesReadUseCase

        every {
            observeDailyPagesReadUseCase(any())
        } answers {
            flowOf(
                DailyPages(
                    year = firstArg(),
                    pages = emptyMap(),
                ),
            )
        }
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
            val daily = DailyPages(
                year = 2025,
                pages = mapOf(
                    LocalDate(
                        year = 2025,
                        month = 4,
                        day = 14,
                    ) to 62,
                ),
            )

            every {
                observeDailyPagesReadUseCase(2025)
            } returns flowOf(daily)

            // ----- Act -----
            val job = launch {
                YearGridCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            stateFlow.value.yearGrid shouldBe yearGrid(daily)
            stateFlow.value.yearAtAGlanceHeader shouldBe yearAtAGlanceHeader(year = 2025)
            job.cancel()
        }

        @Test
        fun `queries the year of today for a rolling window`() = runTest(UnconfinedTestDispatcher()) {
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
        fun `queries the year of today for all time`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = stateFor(StatsScope.AllTime)

            // ----- Act -----
            val job = launch {
                YearGridCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            stateFlow.value.yearGrid.year shouldBe 2026
            job.cancel()
        }

        @Test
        fun `does not query the days again when the scope changes within the same year`() =
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
                    observeDailyPagesReadUseCase(2026)
                }
                job.cancel()
            }

        @Test
        fun `queries the new year when the scope moves to another year`() = runTest(UnconfinedTestDispatcher()) {
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
}
