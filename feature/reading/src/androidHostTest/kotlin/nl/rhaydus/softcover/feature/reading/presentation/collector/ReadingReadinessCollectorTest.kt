package nl.rhaydus.softcover.feature.reading.presentation.collector

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.bookcard.BookCardSet
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.component.header.PageMastheadUiModel
import nl.rhaydus.softcover.core.component.statistic.StatNumberFormat
import nl.rhaydus.softcover.core.component.statistic.StatNumberSuffix
import nl.rhaydus.softcover.core.component.statistic.StatNumberUiModel
import nl.rhaydus.softcover.core.component.statistic.StatNumberVariant
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.mapper.ALSO_READING_STAND_IN_CARDS
import nl.rhaydus.softcover.feature.reading.presentation.mapper.FEATURED_STAND_IN_BOOK
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenDependencies
import nl.rhaydus.softcover.feature.reading.presentation.state.FeaturedProgressUiModel
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.softcover.feature.reading.presentation.state.StreakStripUiModel
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ReadingReadinessCollectorTest {
    private lateinit var dependencies: ReadingScreenDependencies
    private lateinit var stateFlow: MutableStateFlow<ReadingScreenUiState>
    private lateinit var scope: ActionScope<ReadingScreenUiState, ReadingScreenEvent, ReadingLocalVariables>

    @BeforeEach
    fun setUp() {
        stateFlow = MutableStateFlow(ReadingScreenUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(ReadingLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
        dependencies = mockk<ReadingScreenDependencies>(relaxed = true)
    }

    private fun cover(): CoverUiModel = CoverUiModel(
        source = null,
        coverlessTitle = "Title",
        variant = CoverVariant.Raised,
    )

    private fun loadedAlsoReading(): BookCardSet<Nothing> {
        val loadedCard = ALSO_READING_STAND_IN_CARDS.first().copy(isLoading = false)

        return BookCardSet.standIns(persistentListOf(loadedCard))
    }

    private fun loadedProgress(): FeaturedProgressUiModel = FeaturedProgressUiModel(
        percent = StatNumberUiModel(
            value = 10.0,
            format = StatNumberFormat.Plain,
            variant = StatNumberVariant.Headline,
            suffix = StatNumberSuffix.Fixed("%"),
        ),
        pageLead = StatNumberUiModel(
            value = 10.0,
            format = StatNumberFormat.Plain,
            variant = StatNumberVariant.BodyStrong,
            prefix = "Page ",
        ),
        pageTail = " of 100",
        fraction = 0.1f,
    )

    private fun masthead(): PageMastheadUiModel = PageMastheadUiModel(title = "Good morning.")

    private fun readyState(): ReadingScreenUiState = ReadingScreenUiState(
        books = listOf(FEATURED_STAND_IN_BOOK),
        hasLoadedBooks = true,
        hasLoadedReadingActivity = true,
        featuredCover = cover(),
        featuredBackdropCover = cover(),
        featuredProgress = loadedProgress(),
        alsoReading = loadedAlsoReading(),
        masthead = masthead(),
        compactMasthead = masthead(),
    )

    private fun launchCollector(testScope: TestScope): Job = testScope.launch {
        ReadingReadinessCollector().onLaunch(
            scope = scope,
            dependencies = dependencies,
        )
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `clears isLoading once every gate is met`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = readyState()

            // ----- Act -----
            val job = launchCollector(this)

            // ----- Assert -----
            stateFlow.value.isLoading shouldBe false
            job.cancel()
        }

        @Test
        fun `clears the streak strip loading flag together with isLoading`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = readyState().copy(
                streakStrip = StreakStripUiModel(isLoading = true),
            )

            // ----- Act -----
            val job = launchCollector(this)

            // ----- Assert -----
            stateFlow.value.streakStrip.isLoading shouldBe false
            job.cancel()
        }

        @Test
        fun `clears the running figure loading flag together with isLoading`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = readyState().copy(
                streakStrip = StreakStripUiModel(
                    running = StatNumberUiModel(
                        value = 3.0,
                        isLoading = true,
                    ),
                    isLoading = true,
                ),
            )

            // ----- Act -----
            val job = launchCollector(this)

            // ----- Assert -----
            stateFlow.value.streakStrip.running.shouldNotBeNull().isLoading shouldBe false
            job.cancel()
        }

        @Test
        fun `leaves an absent running figure absent`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = readyState().copy(
                streakStrip = StreakStripUiModel(isLoading = true),
            )

            // ----- Act -----
            val job = launchCollector(this)

            // ----- Assert -----
            stateFlow.value.streakStrip.running.shouldBeNull()
            job.cancel()
        }

        @Test
        fun `keeps the streak strip loading while a gate is unmet`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = readyState().copy(
                hasLoadedBooks = false,
                streakStrip = StreakStripUiModel(isLoading = true),
            )

            // ----- Act -----
            val job = launchCollector(this)

            // ----- Assert -----
            stateFlow.value.streakStrip.isLoading shouldBe true
            job.cancel()
        }

        @Test
        fun `stays loading until books have loaded`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = readyState().copy(hasLoadedBooks = false)

            // ----- Act -----
            val job = launchCollector(this)

            // ----- Assert -----
            stateFlow.value.isLoading shouldBe true
            job.cancel()
        }

        @Test
        fun `stays loading while the featured cover is missing`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = readyState().copy(featuredCover = null)

            // ----- Act -----
            val job = launchCollector(this)

            // ----- Assert -----
            stateFlow.value.isLoading shouldBe true
            job.cancel()
        }

        @Test
        fun `stays loading while the featured backdrop cover is missing`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = readyState().copy(featuredBackdropCover = null)

            // ----- Act -----
            val job = launchCollector(this)

            // ----- Assert -----
            stateFlow.value.isLoading shouldBe true
            job.cancel()
        }

        @Test
        fun `stays loading while the featured progress is still the loading stand-in`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                stateFlow.value = readyState().copy(featuredProgress = FeaturedProgressUiModel.Loading)

                // ----- Act -----
                val job = launchCollector(this)

                // ----- Assert -----
                stateFlow.value.isLoading shouldBe true
                job.cancel()
            }

        @Test
        fun `stays loading while an also-reading card is still loading`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = readyState().copy(alsoReading = BookCardSet.standIns(ALSO_READING_STAND_IN_CARDS))

            // ----- Act -----
            val job = launchCollector(this)

            // ----- Assert -----
            stateFlow.value.isLoading shouldBe true
            job.cancel()
        }

        @Test
        fun `stays loading while the masthead is not built`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = readyState().copy(masthead = null)

            // ----- Act -----
            val job = launchCollector(this)

            // ----- Assert -----
            stateFlow.value.isLoading shouldBe true
            job.cancel()
        }

        @Test
        fun `stays loading while the compact masthead is not built`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = readyState().copy(compactMasthead = null)

            // ----- Act -----
            val job = launchCollector(this)

            // ----- Assert -----
            stateFlow.value.isLoading shouldBe true
            job.cancel()
        }

        @Test
        fun `clears for empty books as soon as they have loaded`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ReadingScreenUiState(
                books = emptyList(),
                hasLoadedBooks = true,
                hasLoadedReadingActivity = true,
            )

            // ----- Act -----
            val job = launchCollector(this)

            // ----- Assert -----
            stateFlow.value.isLoading shouldBe false
            job.cancel()
        }

        @Test
        fun `stays loading for empty books until they have loaded`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ReadingScreenUiState(
                books = emptyList(),
                hasLoadedBooks = false,
            )

            // ----- Act -----
            val job = launchCollector(this)

            // ----- Assert -----
            stateFlow.value.isLoading shouldBe true
            job.cancel()
        }

        @Test
        fun `stays loading while reading activity has not loaded`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = readyState().copy(hasLoadedReadingActivity = false)

            // ----- Act -----
            val job = launchCollector(this)

            // ----- Assert -----
            stateFlow.value.isLoading shouldBe true
            job.cancel()
        }

        @Test
        fun `stays loading for empty books until reading activity has loaded`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ReadingScreenUiState(
                books = emptyList(),
                hasLoadedBooks = true,
                hasLoadedReadingActivity = false,
            )

            // ----- Act -----
            val job = launchCollector(this)

            // ----- Assert -----
            stateFlow.value.isLoading shouldBe true
            job.cancel()
        }

        @Test
        fun `clears when reading activity loads after every other gate is met`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = readyState().copy(hasLoadedReadingActivity = false)
            val job = launchCollector(this)

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(hasLoadedReadingActivity = true)

            // ----- Assert -----
            stateFlow.value.isLoading shouldBe false
            job.cancel()
        }

        @Test
        fun `clears when the last missing gate is met later`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = readyState().copy(compactMasthead = null)
            val job = launchCollector(this)

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(compactMasthead = masthead())

            // ----- Assert -----
            stateFlow.value.isLoading shouldBe false
            job.cancel()
        }

        @Test
        fun `clears exactly once and does not re-arm when the state changes afterwards`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                stateFlow.value = readyState()
                val job = launchCollector(this)

                // ----- Act -----
                stateFlow.value = stateFlow.value.copy(
                    isLoading = true,
                    masthead = null,
                )
                stateFlow.value = stateFlow.value.copy(masthead = masthead())

                // ----- Assert -----
                job.isCompleted shouldBe true
                stateFlow.value.isLoading shouldBe true
            }
    }
}
