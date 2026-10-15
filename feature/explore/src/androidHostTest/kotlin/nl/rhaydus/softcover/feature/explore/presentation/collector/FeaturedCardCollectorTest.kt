package nl.rhaydus.softcover.feature.explore.presentation.collector

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.badge.BadgeTone
import nl.rhaydus.softcover.core.component.badge.BadgeUiModel
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class FeaturedCardCollectorTest {
    private lateinit var dependencies: ExploreDependencies
    private lateinit var stateFlow: MutableStateFlow<ExploreScreenUiState>
    private lateinit var scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>

    private val header = SectionHeaderUiModel.Inline(eyebrow = "Most anticipated")

    private val badge = BadgeUiModel(
        label = "Arriving 12 Mar",
        tone = BadgeTone.Primary,
    )

    @BeforeEach
    fun setUp() {
        stateFlow = MutableStateFlow(ExploreScreenUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(ExploreLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
        dependencies = mockk<ExploreDependencies>(relaxed = true)
    }

    private fun book(title: String = "Piranesi"): Book = Book(
        id = 7,
        title = title,
        editions = emptyList(),
        defaultEdition = null,
        rating = 0.0,
        description = "",
        releaseYear = 2030,
        coverUrl = "",
        authors = emptyList(),
        usersCount = 0,
        ratingsCount = 0,
        bookSeries = null,
        positionsInSeries = emptyList(),
        isCompilation = false,
        userBook = null,
    )

    @Nested
    inner class OnLaunch {
        @Test
        fun `shows a masked stand-in under the current header while the release loads`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val job = launch {
                    FeaturedCardCollector().onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Act -----
                stateFlow.value = stateFlow.value.copy(
                    featuredUpcomingRelease = null,
                    loadingFeaturedUpcomingRelease = true,
                    featuredHeader = header,
                )

                // ----- Assert -----
                val card = stateFlow.value.featuredCard.shouldNotBeNull()
                card.isLoading shouldBe true
                card.header shouldBe header
                job.cancel()
            }

        @Test
        fun `collapses the card once the release resolved with nothing to feature`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val job = launch {
                    FeaturedCardCollector().onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Act -----
                stateFlow.value = stateFlow.value.copy(
                    featuredUpcomingRelease = null,
                    loadingFeaturedUpcomingRelease = false,
                )

                // ----- Assert -----
                stateFlow.value.featuredCard.shouldBeNull()
                job.cancel()
            }

        @Test
        fun `builds the real card from the release, cover, badge and header`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val job = launch {
                FeaturedCardCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }
            val cover = mockk<CoverUiModel>()

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(
                featuredUpcomingRelease = book(title = "Piranesi"),
                loadingFeaturedUpcomingRelease = false,
                featuredCover = cover,
                featuredReleaseBadge = badge,
                featuredHeader = header,
            )

            // ----- Assert -----
            val card = stateFlow.value.featuredCard.shouldNotBeNull()
            card.isLoading shouldBe false
            card.title shouldBe "Piranesi"
            card.cover shouldBe cover
            card.badge shouldBe badge
            card.header shouldBe header
            job.cancel()
        }

        @Test
        fun `keeps the real card when a release is present even while still flagged loading`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val job = launch {
                    FeaturedCardCollector().onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Act -----
                stateFlow.value = stateFlow.value.copy(
                    featuredUpcomingRelease = book(title = "Piranesi"),
                    loadingFeaturedUpcomingRelease = true,
                )

                // ----- Assert -----
                val card = stateFlow.value.featuredCard.shouldNotBeNull()
                card.isLoading shouldBe false
                card.title shouldBe "Piranesi"
                job.cancel()
            }

        @Test
        fun `fills in the cover once it arrives after the release`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val job = launch {
                FeaturedCardCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }
            stateFlow.value = stateFlow.value.copy(
                featuredUpcomingRelease = book(),
                loadingFeaturedUpcomingRelease = false,
            )
            val before = stateFlow.value.featuredCard.shouldNotBeNull().cover
            val cover = mockk<CoverUiModel>()

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(featuredCover = cover)

            // ----- Assert -----
            before.shouldBeNull()
            stateFlow.value.featuredCard.shouldNotBeNull().cover shouldBe cover
            job.cancel()
        }

        @Test
        fun `unrelated state change does not rebuild the card`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = stateFlow.value.copy(
                featuredUpcomingRelease = book(),
                loadingFeaturedUpcomingRelease = false,
            )
            val job = launch {
                FeaturedCardCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }
            val cardAfterFirstEmit = stateFlow.value.featuredCard.shouldNotBeNull()

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(searchText = "fantasy")

            // ----- Assert -----
            stateFlow.value.featuredCard.shouldNotBeNull() shouldBeSameInstanceAs cardAfterFirstEmit
            job.cancel()
        }
    }
}
