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
import nl.rhaydus.softcover.core.profile.domain.model.GenreBreakdown
import nl.rhaydus.softcover.core.profile.domain.model.RatingBand
import nl.rhaydus.softcover.core.profile.domain.model.RatingsDistribution
import nl.rhaydus.softcover.core.profile.domain.model.ReadingLife
import nl.rhaydus.softcover.core.profile.domain.usecase.ObserveReadingLifeUseCase
import nl.rhaydus.softcover.feature.profile.presentation.event.StatsAtlasEvent
import nl.rhaydus.softcover.feature.profile.presentation.mapper.toRatingsHistogramCaption
import nl.rhaydus.softcover.feature.profile.presentation.mapper.toRatingsHistogramHeader
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.StatsAtlasDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasLocalVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class AtlasReadingLifeCollectorTest {
    private lateinit var observeReadingLifeUseCase: ObserveReadingLifeUseCase
    private lateinit var stateFlow: MutableStateFlow<StatsAtlasUiState>
    private lateinit var eventChannel: Channel<StatsAtlasEvent>
    private lateinit var scope: ActionScope<StatsAtlasUiState, StatsAtlasEvent, StatsAtlasLocalVariables>
    private lateinit var readingLifeFlow: MutableSharedFlow<ReadingLife?>

    @BeforeEach
    fun setUp() {
        readingLifeFlow = MutableSharedFlow()
        observeReadingLifeUseCase = mockk()
        stateFlow = MutableStateFlow(StatsAtlasUiState())
        eventChannel = Channel(Channel.BUFFERED)
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(StatsAtlasLocalVariables()),
            eventChannel = eventChannel,
        )

        every {
            observeReadingLifeUseCase()
        } returns readingLifeFlow
    }

    private fun stubDependencies(testScope: TestScope): StatsAtlasDependencies {
        val dispatcher = UnconfinedTestDispatcher(testScope.testScheduler)
        return mockk<StatsAtlasDependencies>(relaxed = true).also { mock ->
            every {
                mock.observeReadingLifeUseCase
            } returns observeReadingLifeUseCase

            every {
                mock.coroutineScope
            } returns testScope

            every {
                mock.mainDispatcher
            } returns dispatcher
        }
    }

    private fun buildReadingLife(
        trackedYears: Int = 3,
        ratingBand: RatingBand = RatingBand.NEW_READER,
    ): ReadingLife = ReadingLife(
        booksByYear = emptyList(),
        pagesByYear = emptyList(),
        pagesByMonth = emptyList(),
        genres = GenreBreakdown(),
        ratings = RatingsDistribution(band = ratingBand),
        recentlyLoved = emptyList(),
        trackedYears = trackedYears,
    )

    @Nested
    inner class OnLaunch {
        @Test
        fun `sets readingLife when observe emits non-null`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val readingLife = buildReadingLife()
            val dependencies = stubDependencies(this)
            val collector = AtlasReadingLifeCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            readingLifeFlow.emit(readingLife)

            // ----- Assert -----
            stateFlow.value.readingLife shouldBe readingLife
            job.cancel()
        }

        @Test
        fun `does not update state when observe emits null`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)
            val collector = AtlasReadingLifeCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            readingLifeFlow.emit(null)

            // ----- Assert -----
            stateFlow.value.readingLife shouldBe null
            job.cancel()
        }

        @Test
        fun `sets ratingsHistogramHeader and ratingsHistogramCaption from the emitted ratings band`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val readingLife = buildReadingLife(ratingBand = RatingBand.SUPERFAN)
                val dependencies = stubDependencies(this)
                val collector = AtlasReadingLifeCollector()
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Act -----
                readingLifeFlow.emit(readingLife)

                // ----- Assert -----
                stateFlow.value.ratingsHistogramHeader shouldBe RatingBand.SUPERFAN.toRatingsHistogramHeader()
                stateFlow.value.ratingsHistogramCaption shouldBe RatingBand.SUPERFAN.toRatingsHistogramCaption()
                job.cancel()
            }
    }
}
