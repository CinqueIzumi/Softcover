package nl.rhaydus.softcover.feature.profile.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.TimeZone
import nl.rhaydus.softcover.core.profile.domain.model.GenreBreakdown
import nl.rhaydus.softcover.core.profile.domain.model.RatingsDistribution
import nl.rhaydus.softcover.core.profile.domain.model.ReadingLife
import nl.rhaydus.softcover.core.profile.domain.model.YearCount
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveReadingLifeUseCase
import nl.rhaydus.softcover.feature.profile.presentation.event.ProfileEvent
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.ProfileDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.LocalProfileVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.ProfileUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.time.Clock
import kotlin.time.Instant

class ReadingSummaryCollectorTest {
    private lateinit var observeReadingLifeUseCase: ObserveReadingLifeUseCase
    private lateinit var stateFlow: MutableStateFlow<ProfileUiState>
    private lateinit var scope: ActionScope<ProfileUiState, ProfileEvent, LocalProfileVariables>
    private lateinit var readingLifeFlow: MutableSharedFlow<ReadingLife?>

    private val readingLife = ReadingLife(
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
        pagesByMonth = emptyList(),
        genres = GenreBreakdown(),
        ratings = RatingsDistribution(),
        recentlyLoved = emptyList(),
    )

    @BeforeEach
    fun setUp() {
        readingLifeFlow = MutableSharedFlow()
        observeReadingLifeUseCase = mockk()
        stateFlow = MutableStateFlow(ProfileUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LocalProfileVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        every {
            observeReadingLifeUseCase()
        } returns readingLifeFlow
    }

    private fun stubDependencies(
        testScope: TestScope,
        now: Instant,
        timeZone: TimeZone = TimeZone.UTC,
    ): ProfileDependencies {
        val dispatcher = UnconfinedTestDispatcher(testScope.testScheduler)
        val fixedClock = object : Clock {
            override fun now(): Instant = now
        }
        return mockk<ProfileDependencies>(relaxed = true).also { mock ->
            every {
            mock.observeReadingLifeUseCase
        } returns observeReadingLifeUseCase
            every {
            mock.clock
        } returns fixedClock
            every {
            mock.timeZone
        } returns timeZone
            every {
            mock.coroutineScope
        } returns testScope
            every {
            mock.mainDispatcher
        } returns dispatcher
        }
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `summarises the year the clock is in on the last second of the old year`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val dependencies = stubDependencies(
                    this,
                    now = Instant.parse("2024-12-31T23:59:59Z"),
                )
                val collector = ReadingSummaryCollector()
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Act -----
                readingLifeFlow.emit(readingLife)

                // ----- Assert -----
                stateFlow.value.readingSummary.stats.map { it.stat.value } shouldBe listOf(9.0, 800.0)
                job.cancel()
            }

        @Test
        fun `summarises the new year the clock is in right after New Year`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val dependencies = stubDependencies(
                    this,
                    now = Instant.parse("2025-01-01T00:00:00Z"),
                )
                val collector = ReadingSummaryCollector()
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Act -----
                readingLifeFlow.emit(readingLife)

                // ----- Assert -----
                stateFlow.value.readingSummary.stats.map { it.stat.value } shouldBe listOf(4.0, 1_200.0)
                job.cancel()
            }

        @Test
        fun `reads the year in the injected time zone, not in UTC`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val dependencies = stubDependencies(
                testScope = this,
                now = Instant.parse("2024-12-31T23:30:00Z"),
                timeZone = TimeZone.of("Pacific/Auckland"),
            )
            val collector = ReadingSummaryCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            readingLifeFlow.emit(readingLife)

            // ----- Assert -----
            stateFlow.value.readingSummary.stats.map { it.stat.value } shouldBe listOf(4.0, 1_200.0)
            job.cancel()
        }

        @Test
        fun `keeps the loading summary when observe emits null`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val dependencies = stubDependencies(
                this,
                now = Instant.parse("2025-06-01T00:00:00Z"),
            )
            val collector = ReadingSummaryCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            readingLifeFlow.emit(null)

            // ----- Assert -----
            stateFlow.value.readingSummary.stats.map { it.stat.isLoading } shouldBe listOf(true, true)
            job.cancel()
        }
    }
}
