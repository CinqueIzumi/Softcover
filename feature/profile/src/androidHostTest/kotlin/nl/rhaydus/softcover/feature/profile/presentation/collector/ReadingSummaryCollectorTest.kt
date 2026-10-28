package nl.rhaydus.softcover.feature.profile.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.domain.util.ObserveCurrentDateUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.ObserveReadingStatsBackfilledUseCase
import nl.rhaydus.softcover.core.profile.domain.model.ReadingActivity
import nl.rhaydus.softcover.core.profile.domain.model.ReadingHistory
import nl.rhaydus.softcover.core.profile.domain.model.ReadingStreak
import nl.rhaydus.softcover.core.profile.domain.model.YearCount
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveReadingActivityUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveReadingHistoryUseCase
import nl.rhaydus.softcover.feature.profile.presentation.event.ProfileEvent
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.ProfileDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.LocalProfileVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.ProfileUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ReadingSummaryCollectorTest {
    private lateinit var dependencies: ProfileDependencies
    private lateinit var stateFlow: MutableStateFlow<ProfileUiState>
    private lateinit var scope: ActionScope<ProfileUiState, ProfileEvent, LocalProfileVariables>
    private lateinit var historyFlow: MutableStateFlow<ReadingHistory>
    private lateinit var activityFlow: MutableStateFlow<ReadingActivity>
    private lateinit var backfilledFlow: MutableStateFlow<Boolean>
    private lateinit var dateFlow: MutableStateFlow<LocalDate>

    private val newYearsEve = LocalDate(
        year = 2024,
        month = 12,
        day = 31,
    )

    private val readingHistory = ReadingHistory(
        booksByYear = listOf(
            YearCount(
                year = 2024,
                count = 9,
            ),
            YearCount(
                year = 2025,
                count = 4,
            ),
        ),
        pagesByYear = listOf(
            YearCount(
                year = 2024,
                count = 800,
            ),
            YearCount(
                year = 2025,
                count = 1_200,
            ),
        ),
    )

    @BeforeEach
    fun setUp() {
        historyFlow = MutableStateFlow(ReadingHistory())
        activityFlow = MutableStateFlow(
            ReadingActivity(
                pagesByDay = emptyMap(),
                streaks = emptyList(),
            ),
        )
        backfilledFlow = MutableStateFlow(true)
        dateFlow = MutableStateFlow(newYearsEve)
        stateFlow = MutableStateFlow(ProfileUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LocalProfileVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        val observeReadingHistoryUseCase = mockk<ObserveReadingHistoryUseCase>()
        val observeReadingActivityUseCase = mockk<ObserveReadingActivityUseCase>()
        val observeReadingStatsBackfilledUseCase = mockk<ObserveReadingStatsBackfilledUseCase>()
        val observeCurrentDateUseCase = mockk<ObserveCurrentDateUseCase>()

        every {
            observeReadingHistoryUseCase()
        } returns historyFlow

        every {
            observeReadingActivityUseCase()
        } returns activityFlow

        every {
            observeReadingStatsBackfilledUseCase()
        } returns backfilledFlow

        every {
            observeCurrentDateUseCase()
        } returns dateFlow

        dependencies = mockk(relaxed = true)

        every {
            dependencies.observeReadingHistoryUseCase
        } returns observeReadingHistoryUseCase

        every {
            dependencies.observeReadingActivityUseCase
        } returns observeReadingActivityUseCase

        every {
            dependencies.observeReadingStatsBackfilledUseCase
        } returns observeReadingStatsBackfilledUseCase

        every {
            dependencies.observeCurrentDateUseCase
        } returns observeCurrentDateUseCase
    }

    private fun activityWithStreak(streak: ReadingStreak): ReadingActivity = ReadingActivity(
        pagesByDay = emptyMap(),
        streaks = listOf(streak),
    )

    @Nested
    inner class OnLaunch {
        @Test
        fun `summarises the year of the current date on its last day`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = ReadingSummaryCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            historyFlow.value = readingHistory

            // ----- Assert -----
            stateFlow.value.readingSummary.stats.map { it.stat.value } shouldBe listOf(9.0, 800.0, 0.0)
            job.cancel()
        }

        @Test
        fun `moves to the new year when the date rolls over`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            historyFlow.value = readingHistory
            val collector = ReadingSummaryCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            dateFlow.value = LocalDate(
                year = 2025,
                month = 1,
                day = 1,
            )

            // ----- Assert -----
            stateFlow.value.readingSummary.stats.map { it.stat.value } shouldBe listOf(4.0, 1_200.0, 0.0)
            job.cancel()
        }

        @Test
        fun `keeps the loading summary while the library is not backfilled`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            backfilledFlow.value = false
            val collector = ReadingSummaryCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            historyFlow.value = readingHistory

            // ----- Assert -----
            stateFlow.value.readingSummary.stats.map { it.stat.isLoading } shouldBe listOf(true, true, true)
            stateFlow.value.readingSummary.recentDays.isLoading shouldBe true
            job.cancel()
        }

        @Test
        fun `lifts the loading summary once the library is backfilled`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            backfilledFlow.value = false
            val collector = ReadingSummaryCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            backfilledFlow.value = true

            // ----- Assert -----
            stateFlow.value.readingSummary.stats.map { it.stat.isLoading } shouldBe listOf(false, false, false)
            job.cancel()
        }
    }

    @Nested
    inner class CurrentStreakDays {
        @Test
        fun `writes the length of a run ending today`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = ReadingSummaryCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            activityFlow.value = activityWithStreak(
                ReadingStreak(
                    start = LocalDate(
                        year = 2024,
                        month = 12,
                        day = 28,
                    ),
                    end = newYearsEve,
                ),
            )

            // ----- Assert -----
            stateFlow.value.currentStreakDays shouldBe 4
            job.cancel()
        }

        @Test
        fun `drops to zero when the date moves past the day after the run`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            activityFlow.value = activityWithStreak(
                ReadingStreak(
                    start = LocalDate(
                        year = 2024,
                        month = 12,
                        day = 29,
                    ),
                    end = newYearsEve,
                ),
            )
            val collector = ReadingSummaryCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            dateFlow.value = LocalDate(
                year = 2025,
                month = 1,
                day = 2,
            )

            // ----- Assert -----
            stateFlow.value.currentStreakDays shouldBe 0
            job.cancel()
        }

        @Test
        fun `stays zero while the library is not backfilled`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            backfilledFlow.value = false
            val collector = ReadingSummaryCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            activityFlow.value = activityWithStreak(
                ReadingStreak(
                    start = newYearsEve,
                    end = newYearsEve,
                ),
            )

            // ----- Assert -----
            stateFlow.value.currentStreakDays shouldBe 0
            job.cancel()
        }
    }
}
