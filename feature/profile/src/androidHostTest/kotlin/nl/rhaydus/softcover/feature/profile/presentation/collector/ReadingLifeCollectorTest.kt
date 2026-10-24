package nl.rhaydus.softcover.feature.profile.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.StatsRange
import nl.rhaydus.softcover.core.preferences.domain.usecase.ObserveAuthorDemographicsBackfilledUseCase
import nl.rhaydus.softcover.core.profile.domain.model.ReadingHistory
import nl.rhaydus.softcover.core.profile.domain.model.ReadingStats
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveReadingHistoryUseCase
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveReadingStatsUseCase
import nl.rhaydus.softcover.feature.profile.presentation.event.ProfileEvent
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.ProfileDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.LocalProfileVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.ProfileUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ReadingLifeCollectorTest {
    private lateinit var observeReadingStatsUseCase: ObserveReadingStatsUseCase
    private lateinit var observeReadingHistoryUseCase: ObserveReadingHistoryUseCase
    private lateinit var observeAuthorDemographicsBackfilledUseCase: ObserveAuthorDemographicsBackfilledUseCase
    private lateinit var stateFlow: MutableStateFlow<ProfileUiState>
    private lateinit var eventChannel: Channel<ProfileEvent>
    private lateinit var scope: ActionScope<ProfileUiState, ProfileEvent, LocalProfileVariables>
    private lateinit var readingStatsFlow: MutableStateFlow<ReadingStats>
    private lateinit var readingHistoryFlow: MutableStateFlow<ReadingHistory>
    private lateinit var backfilledFlow: MutableStateFlow<Boolean>

    @BeforeEach
    fun setUp() {
        readingStatsFlow = MutableStateFlow(ReadingStats())
        readingHistoryFlow = MutableStateFlow(ReadingHistory())
        backfilledFlow = MutableStateFlow(true)
        observeReadingStatsUseCase = mockk()
        observeReadingHistoryUseCase = mockk()
        observeAuthorDemographicsBackfilledUseCase = mockk()
        stateFlow = MutableStateFlow(ProfileUiState())
        eventChannel = Channel(Channel.BUFFERED)
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LocalProfileVariables()),
            eventChannel = eventChannel,
        )

        every {
            observeReadingStatsUseCase(StatsRange.Unbounded)
        } returns readingStatsFlow

        every {
            observeReadingHistoryUseCase()
        } returns readingHistoryFlow

        every {
            observeAuthorDemographicsBackfilledUseCase()
        } returns backfilledFlow
    }

    private fun stubDependencies(testScope: TestScope): ProfileDependencies {
        val dispatcher = UnconfinedTestDispatcher(testScope.testScheduler)
        return mockk<ProfileDependencies>(relaxed = true).also { mock ->
            every {
                mock.observeReadingStatsUseCase
            } returns observeReadingStatsUseCase

            every {
                mock.observeReadingHistoryUseCase
            } returns observeReadingHistoryUseCase

            every {
                mock.observeAuthorDemographicsBackfilledUseCase
            } returns observeAuthorDemographicsBackfilledUseCase

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
        fun `updates the reading stats and history once the library is backfilled`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val stats = ReadingStats(
                    booksRead = 12,
                    totalPages = 3400,
                )
                val history = ReadingHistory(trackedYears = 3)
                val dependencies = stubDependencies(this)
                val collector = ReadingLifeCollector()
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Act -----
                readingStatsFlow.value = stats
                readingHistoryFlow.value = history

                // ----- Assert -----
                stateFlow.value.readingStats shouldBe stats
                stateFlow.value.readingHistory shouldBe history
                job.cancel()
            }

        @Test
        fun `leaves the reading stats and history unset while the library is not backfilled`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                backfilledFlow.value = false
                val dependencies = stubDependencies(this)
                val collector = ReadingLifeCollector()
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Act -----
                readingStatsFlow.value = ReadingStats(booksRead = 12)

                // ----- Assert -----
                stateFlow.value.readingStats shouldBe null
                stateFlow.value.readingHistory shouldBe null
                job.cancel()
            }
    }
}
