package nl.rhaydus.softcover.feature.profile.presentation.collector

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.bookcard.BookCardKey
import nl.rhaydus.softcover.core.profile.domain.model.GenreBreakdown
import nl.rhaydus.softcover.core.profile.domain.model.LovedBook
import nl.rhaydus.softcover.core.profile.domain.model.RatingsDistribution
import nl.rhaydus.softcover.core.profile.domain.model.ReadingLife
import nl.rhaydus.softcover.feature.profile.presentation.event.ProfileEvent
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.ProfileDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.LocalProfileVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.ProfileUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class RecentlyLovedCardsCollectorTest {
    private lateinit var stateFlow: MutableStateFlow<ProfileUiState>
    private lateinit var eventChannel: Channel<ProfileEvent>
    private lateinit var scope: ActionScope<ProfileUiState, ProfileEvent, LocalProfileVariables>

    @BeforeEach
    fun setUp() {
        stateFlow = MutableStateFlow(ProfileUiState())
        eventChannel = Channel(Channel.BUFFERED)
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LocalProfileVariables()),
            eventChannel = eventChannel,
        )
    }

    private fun lovedBooks(count: Int): List<LovedBook> = List(count) {
        LovedBook(
            bookId = it + 1,
            coverUrl = "cover$it.jpg",
            title = "Title $it",
            author = "Author $it",
            ratingStars = 4.0,
            monthLabel = "May 2026",
        )
    }

    private fun readingLife(recentlyLoved: List<LovedBook>): ReadingLife = ReadingLife(
        booksByYear = emptyList(),
        pagesByYear = emptyList(),
        pagesByMonth = emptyList(),
        genres = GenreBreakdown(),
        ratings = RatingsDistribution(),
        recentlyLoved = recentlyLoved,
    )

    @Nested
    inner class OnLaunch {
        @Test
        fun `shows loading stand-ins while reading life has not loaded`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = RecentlyLovedCardsCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = mockk<ProfileDependencies>(relaxed = true),
                )
            }

            // ----- Assert -----
            stateFlow.value.recentlyLovedCards.shouldNotBeEmpty()
            stateFlow.value.recentlyLovedCards.forEach { it.isLoading shouldBe true }
            job.cancel()
        }

        @Test
        fun `produces no cards once reading life has landed with nothing loved`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ProfileUiState(
                isLoading = false,
                readingLife = readingLife(emptyList()),
            )
            val collector = RecentlyLovedCardsCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = mockk<ProfileDependencies>(relaxed = true),
                )
            }

            // ----- Assert -----
            stateFlow.value.recentlyLovedCards.shouldBeEmpty()
            job.cancel()
        }

        @Test
        fun `maps every book when there are fewer than the cap`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ProfileUiState(readingLife = readingLife(lovedBooks(2)))
            val collector = RecentlyLovedCardsCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = mockk<ProfileDependencies>(relaxed = true),
                )
            }

            // ----- Assert -----
            stateFlow.value.recentlyLovedCards.map { it.key } shouldBe listOf(
                BookCardKey.Book(bookId = 1),
                BookCardKey.Book(bookId = 2),
            )
            job.cancel()
        }

        @Test
        fun `keeps only the first three books in order when more are loved`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ProfileUiState(readingLife = readingLife(lovedBooks(5)))
            val collector = RecentlyLovedCardsCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = mockk<ProfileDependencies>(relaxed = true),
                )
            }

            // ----- Assert -----
            stateFlow.value.recentlyLovedCards.map { it.key } shouldBe listOf(
                BookCardKey.Book(bookId = 1),
                BookCardKey.Book(bookId = 2),
                BookCardKey.Book(bookId = 3),
            )
            job.cancel()
        }

        @Test
        fun `rebuilds the cards when reading life changes`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ProfileUiState(readingLife = readingLife(lovedBooks(1)))
            val collector = RecentlyLovedCardsCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = mockk<ProfileDependencies>(relaxed = true),
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(
                readingLife = readingLife(lovedBooks(3).drop(1)),
            )

            // ----- Assert -----
            stateFlow.value.recentlyLovedCards.map { it.key } shouldBe listOf(
                BookCardKey.Book(bookId = 2),
                BookCardKey.Book(bookId = 3),
            )
            job.cancel()
        }

        @Test
        fun `returns to loading stand-ins when reading life becomes null`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ProfileUiState(readingLife = readingLife(lovedBooks(2)))
            val collector = RecentlyLovedCardsCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = mockk<ProfileDependencies>(relaxed = true),
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(readingLife = null)

            // ----- Assert -----
            stateFlow.value.recentlyLovedCards.shouldNotBeEmpty()
            stateFlow.value.recentlyLovedCards.forEach { it.isLoading shouldBe true }
            job.cancel()
        }

        @Test
        fun `maps the loved books rather than stand-ins while a refresh is still loading`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                stateFlow.value = ProfileUiState(
                    isLoading = true,
                    readingLife = readingLife(lovedBooks(2)),
                )
                val collector = RecentlyLovedCardsCollector()

                // ----- Act -----
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = mockk<ProfileDependencies>(relaxed = true),
                    )
                }

                // ----- Assert -----
                stateFlow.value.recentlyLovedCards.map { it.key } shouldBe listOf(
                    BookCardKey.Book(bookId = 1),
                    BookCardKey.Book(bookId = 2),
                )
                stateFlow.value.recentlyLovedCards.forEach { it.isLoading shouldBe false }
                job.cancel()
            }
    }
}
