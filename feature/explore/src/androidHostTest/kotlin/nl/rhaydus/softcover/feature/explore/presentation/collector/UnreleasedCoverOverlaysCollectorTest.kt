package nl.rhaydus.softcover.feature.explore.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.component.badge.CoverOverlayCorner
import nl.rhaydus.softcover.core.component.badge.CoverOverlayUiModel
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.uibinding.release.UnreleasedBadgeStyle
import nl.rhaydus.softcover.core.uibinding.release.toUnreleasedBadgeUiModel
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

private val RELEASE_DATE = LocalDate(
    2026,
    9,
    20,
)

private val EXPECTED_OVERLAY = CoverOverlayUiModel(
    badge = RELEASE_DATE.toUnreleasedBadgeUiModel(UnreleasedBadgeStyle.Compact),
    corner = CoverOverlayCorner.TopStart,
    grayscale = false,
)

class UnreleasedCoverOverlaysCollectorTest {
    private lateinit var dependencies: ExploreDependencies
    private lateinit var stateFlow: MutableStateFlow<ExploreScreenUiState>
    private lateinit var scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>

    @BeforeEach
    fun setUp() {
        stateFlow = MutableStateFlow(ExploreScreenUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(ExploreLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
        dependencies = mockk(relaxed = true)
    }

    private fun stubBook(
        id: Int,
        isUnreleased: Boolean = true,
        effectiveReleaseDate: LocalDate? = RELEASE_DATE,
    ): Book = mockk {
        every {
            this@mockk.id
        } returns id

        every {
            this@mockk.isUnreleased
        } returns isUnreleased

        every {
            this@mockk.effectiveReleaseDate
        } returns effectiveReleaseDate
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `puts an unreleased book with a release date into unreleasedCoverOverlays keyed by its id`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val book = stubBook(id = 1)
                stateFlow.value = ExploreScreenUiState(trendingBooks = listOf(book))
                val collector = UnreleasedCoverOverlaysCollector()

                // ----- Act -----
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Assert -----
                stateFlow.value.unreleasedCoverOverlays[1] shouldBe EXPECTED_OVERLAY
                job.cancel()
            }

        @Test
        fun `excludes a released book from unreleasedCoverOverlays`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val book = stubBook(
                id = 1,
                isUnreleased = false,
            )
            stateFlow.value = ExploreScreenUiState(trendingBooks = listOf(book))
            val collector = UnreleasedCoverOverlaysCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            stateFlow.value.unreleasedCoverOverlays shouldBe emptyMap()
            job.cancel()
        }

        @Test
        fun `excludes an unreleased book with no release date from unreleasedCoverOverlays`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val book = stubBook(
                id = 1,
                effectiveReleaseDate = null,
            )
            stateFlow.value = ExploreScreenUiState(trendingBooks = listOf(book))
            val collector = UnreleasedCoverOverlaysCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            stateFlow.value.unreleasedCoverOverlays shouldBe emptyMap()
            job.cancel()
        }

        @Test
        fun `collects an unreleased book from becauseYouReadBooks`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val book = stubBook(id = 2)
            stateFlow.value = ExploreScreenUiState(becauseYouReadBooks = listOf(book))
            val collector = UnreleasedCoverOverlaysCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            stateFlow.value.unreleasedCoverOverlays.containsKey(2) shouldBe true
            job.cancel()
        }

        @Test
        fun `collects an unreleased book from continueSeriesBooks`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val book = stubBook(id = 3)
            stateFlow.value = ExploreScreenUiState(continueSeriesBooks = listOf(book))
            val collector = UnreleasedCoverOverlaysCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            stateFlow.value.unreleasedCoverOverlays.containsKey(3) shouldBe true
            job.cancel()
        }

        @Test
        fun `collects an unreleased book from queriedBooks`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val book = stubBook(id = 4)
            stateFlow.value = ExploreScreenUiState(queriedBooks = listOf(book))
            val collector = UnreleasedCoverOverlaysCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            stateFlow.value.unreleasedCoverOverlays.containsKey(4) shouldBe true
            job.cancel()
        }

        @Test
        fun `collapses the same book appearing in two lists into one entry`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val book = stubBook(id = 5)
            stateFlow.value = ExploreScreenUiState(
                trendingBooks = listOf(book),
                becauseYouReadBooks = listOf(book),
            )
            val collector = UnreleasedCoverOverlaysCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            stateFlow.value.unreleasedCoverOverlays.size shouldBe 1
            stateFlow.value.unreleasedCoverOverlays.containsKey(5) shouldBe true
            job.cancel()
        }

        @Test
        fun `featuredReleaseBadge uses the Featured copy and is unaffected by the cover-overlay corner`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val featured = stubBook(id = 6)
                stateFlow.value = ExploreScreenUiState(featuredUpcomingRelease = featured)
                val collector = UnreleasedCoverOverlaysCollector()

                // ----- Act -----
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Assert -----
                stateFlow.value.featuredReleaseBadge shouldBe RELEASE_DATE.toUnreleasedBadgeUiModel(UnreleasedBadgeStyle.Featured)
                job.cancel()
            }

        @Test
        fun `featuredReleaseBadge does not gate on isUnreleased`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val featured = stubBook(
                id = 7,
                isUnreleased = false,
            )
            stateFlow.value = ExploreScreenUiState(featuredUpcomingRelease = featured)
            val collector = UnreleasedCoverOverlaysCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            stateFlow.value.featuredReleaseBadge shouldBe RELEASE_DATE.toUnreleasedBadgeUiModel(UnreleasedBadgeStyle.Featured)
            job.cancel()
        }

        @Test
        fun `featuredReleaseBadge is null when featuredUpcomingRelease is null`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ExploreScreenUiState(featuredUpcomingRelease = null)
            val collector = UnreleasedCoverOverlaysCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            stateFlow.value.featuredReleaseBadge shouldBe null
            job.cancel()
        }

        @Test
        fun `featuredReleaseBadge is null when featuredUpcomingRelease has no release date`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val featured = stubBook(
                id = 8,
                effectiveReleaseDate = null,
            )
            stateFlow.value = ExploreScreenUiState(featuredUpcomingRelease = featured)
            val collector = UnreleasedCoverOverlaysCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            stateFlow.value.featuredReleaseBadge shouldBe null
            job.cancel()
        }
    }
}
