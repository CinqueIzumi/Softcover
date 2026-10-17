package nl.rhaydus.softcover.feature.explore.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.component.badge.CoverOverlayCorner
import nl.rhaydus.softcover.core.component.badge.CoverOverlayUiModel
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.uibinding.release.toCompactUnreleasedBadgeUiModel
import nl.rhaydus.softcover.core.uibinding.release.toFeaturedUnreleasedBadgeUiModel
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
    badge = RELEASE_DATE.toCompactUnreleasedBadgeUiModel(),
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
        releaseDate: LocalDate? = RELEASE_DATE,
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

        every {
            this@mockk.releaseDate
        } returns releaseDate
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `puts an unreleased book with a release date into unreleasedCoverOverlays keyed by its id`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val book = stubBook(id = 1)
                stateFlow.value = ExploreScreenUiState(trendingBooks = persistentListOf(book))
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
            stateFlow.value = ExploreScreenUiState(trendingBooks = persistentListOf(book))
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
            stateFlow.value = ExploreScreenUiState(trendingBooks = persistentListOf(book))
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
        fun `collects unreleased books from every rail and the search results`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ExploreScreenUiState(
                trendingBooks = persistentListOf(stubBook(id = 1)),
                becauseYouReadBooks = persistentListOf(stubBook(id = 2)),
                continueSeriesBooks = persistentListOf(stubBook(id = 3)),
                queriedBooks = persistentListOf(stubBook(id = 4)),
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
            stateFlow.value.unreleasedCoverOverlays.keys shouldBe setOf(1, 2, 3, 4)
            job.cancel()
        }

        @Test
        fun `collapses the same book appearing in two lists into one entry`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val book = stubBook(id = 5)
            stateFlow.value = ExploreScreenUiState(
                trendingBooks = persistentListOf(book),
                becauseYouReadBooks = persistentListOf(book),
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
        fun `featuredReleaseBadge uses the book-level releaseDate over the edition-derived date`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val bookDebut = LocalDate(
                    2026,
                    9,
                    15,
                )
                val editionDate = LocalDate(
                    2026,
                    12,
                    8,
                )
                val featured = stubBook(
                    id = 6,
                    effectiveReleaseDate = editionDate,
                    releaseDate = bookDebut,
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
                stateFlow.value.featuredReleaseBadge shouldBe bookDebut.toFeaturedUnreleasedBadgeUiModel()
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
            stateFlow.value.featuredReleaseBadge shouldBe RELEASE_DATE.toFeaturedUnreleasedBadgeUiModel()
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
                effectiveReleaseDate = RELEASE_DATE,
                releaseDate = null,
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
