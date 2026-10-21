package nl.rhaydus.softcover.feature.library.presentation.collector

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.bookcard.BookCardKey
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.presentation.model.LibraryTab
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class RecommendedRailCollectorTest {
    private val wantToReadTab = LibraryTab.Status.of(UserBookStatus.WANT_TO_READ)

    private fun book(id: Int) = Book(
        id = id,
        canonicalId = null,
        title = "Book $id",
        editions = emptyList(),
        defaultEdition = null,
        rating = 0.0,
        description = "",
        releaseYear = 2020,
        releaseDate = null,
        coverUrl = "",
        authors = emptyList(),
        usersCount = 0,
        ratingsCount = 0,
        bookSeries = null,
        positionsInSeries = emptyList(),
        isCompilation = false,
        userBook = null,
    )

    private fun shelf(count: Int) = (1..count).map { book(id = 100 + it) }.toImmutableList()

    private fun state(
        shelfBooks: List<Book>? = shelf(count = 2),
        recommended: List<Book> = listOf(book(id = 1), book(id = 2)),
        loading: Boolean = false,
        shelfLoading: Boolean = false,
        tabs: List<LibraryTab> = listOf(LibraryTab.All, wantToReadTab),
        selectionMode: Boolean = false,
        isRearranging: Boolean = false,
    ) = LibraryUiState(
        visibleTabs = tabs.toImmutableList(),
        booksByTab = if (shelfBooks == null) {
            persistentMapOf()
        } else {
            persistentMapOf(wantToReadTab.id to shelfBooks.toImmutableList())
        },
        recommendedBooks = recommended.toImmutableList(),
        loadingRecommendedBooks = loading,
        isLoading = shelfLoading,
        selectionMode = selectionMode,
        isRearranging = isRearranging,
    )

    private fun scopeFor(stateFlow: MutableStateFlow<LibraryUiState>) = ActionScope(
        stateFlow = stateFlow,
        localVariablesFlow = MutableStateFlow(LibraryLocalVariables()),
        eventChannel = Channel<LibraryEvent>(Channel.BUFFERED),
    )

    @Nested
    inner class OnLaunch {
        @Test
        fun `publishes the recommended cards in order under the want to read tab when the shelf is short`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val stateFlow = MutableStateFlow(state())
                val job = launch {
                    RecommendedRailCollector().onLaunch(
                        scope = scopeFor(stateFlow),
                        dependencies = mockk<LibraryDependencies>(relaxed = true),
                    )
                }

                // ----- Act -----
                val rails = stateFlow.value.recommendedRailByTab

                // ----- Assert -----
                rails.keys shouldBe setOf(wantToReadTab.id)
                rails.getValue(wantToReadTab.id).cards.map { it.key } shouldBe listOf(
                    BookCardKey.Book(bookId = 1),
                    BookCardKey.Book(bookId = 2),
                )
                job.cancel()
            }

        @Test
        fun `publishes no rail once the shelf holds five books`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val stateFlow = MutableStateFlow(state(shelfBooks = shelf(count = 5)))
            val job = launch {
                RecommendedRailCollector().onLaunch(
                    scope = scopeFor(stateFlow),
                    dependencies = mockk<LibraryDependencies>(relaxed = true),
                )
            }

            // ----- Act -----
            val rails = stateFlow.value.recommendedRailByTab

            // ----- Assert -----
            rails.keys.shouldBeEmpty()
            job.cancel()
        }

        @Test
        fun `publishes no rail in selection mode`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val stateFlow = MutableStateFlow(state(selectionMode = true))
            val job = launch {
                RecommendedRailCollector().onLaunch(
                    scope = scopeFor(stateFlow),
                    dependencies = mockk<LibraryDependencies>(relaxed = true),
                )
            }

            // ----- Act -----
            val rails = stateFlow.value.recommendedRailByTab

            // ----- Assert -----
            rails.keys.shouldBeEmpty()
            job.cancel()
        }

        @Test
        fun `publishes no rail while rearranging`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val stateFlow = MutableStateFlow(state(isRearranging = true))
            val job = launch {
                RecommendedRailCollector().onLaunch(
                    scope = scopeFor(stateFlow),
                    dependencies = mockk<LibraryDependencies>(relaxed = true),
                )
            }

            // ----- Act -----
            val rails = stateFlow.value.recommendedRailByTab

            // ----- Assert -----
            rails.keys.shouldBeEmpty()
            job.cancel()
        }

        @Test
        fun `publishes stand-in cards with no payloads while recommendations load`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val stateFlow = MutableStateFlow(
                    state(
                        recommended = emptyList(),
                        loading = true,
                    ),
                )
                val job = launch {
                    RecommendedRailCollector().onLaunch(
                        scope = scopeFor(stateFlow),
                        dependencies = mockk<LibraryDependencies>(relaxed = true),
                    )
                }

                // ----- Act -----
                val set = stateFlow.value.recommendedRailByTab[wantToReadTab.id].shouldNotBeNull()

                // ----- Assert -----
                set.cards.isNotEmpty() shouldBe true
                set.cards.all { it.isLoading } shouldBe true
                set.payloadByKey.isEmpty() shouldBe true
                job.cancel()
            }

        @Test
        fun `publishes no rail without recommendations once loading has finished`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val stateFlow = MutableStateFlow(
                    state(
                        recommended = emptyList(),
                        loading = false,
                    ),
                )
                val job = launch {
                    RecommendedRailCollector().onLaunch(
                        scope = scopeFor(stateFlow),
                        dependencies = mockk<LibraryDependencies>(relaxed = true),
                    )
                }

                // ----- Act -----
                val rails = stateFlow.value.recommendedRailByTab

                // ----- Assert -----
                rails.keys.shouldBeEmpty()
                job.cancel()
            }

        @Test
        fun `publishes no rail when want to read is not a visible tab`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val stateFlow = MutableStateFlow(state(tabs = listOf(LibraryTab.All)))
            val job = launch {
                RecommendedRailCollector().onLaunch(
                    scope = scopeFor(stateFlow),
                    dependencies = mockk<LibraryDependencies>(relaxed = true),
                )
            }

            // ----- Act -----
            val rails = stateFlow.value.recommendedRailByTab

            // ----- Assert -----
            rails.keys.shouldBeEmpty()
            job.cancel()
        }

        @Test
        fun `publishes no rail before the want to read books have loaded`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val stateFlow = MutableStateFlow(state(shelfBooks = null))
            val job = launch {
                RecommendedRailCollector().onLaunch(
                    scope = scopeFor(stateFlow),
                    dependencies = mockk<LibraryDependencies>(relaxed = true),
                )
            }

            // ----- Act -----
            val rails = stateFlow.value.recommendedRailByTab

            // ----- Assert -----
            rails.keys.shouldBeEmpty()
            job.cancel()
        }

        @Test
        fun `publishes no rail for an empty shelf that is still loading`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val stateFlow = MutableStateFlow(
                state(
                    shelfBooks = emptyList(),
                    shelfLoading = true,
                ),
            )
            val job = launch {
                RecommendedRailCollector().onLaunch(
                    scope = scopeFor(stateFlow),
                    dependencies = mockk<LibraryDependencies>(relaxed = true),
                )
            }

            // ----- Act -----
            val rails = stateFlow.value.recommendedRailByTab

            // ----- Assert -----
            rails.keys.shouldBeEmpty()
            job.cancel()
        }

        @Test
        fun `publishes a rail for an empty shelf that has finished loading`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val stateFlow = MutableStateFlow(
                state(
                    shelfBooks = emptyList(),
                    shelfLoading = false,
                ),
            )
            val job = launch {
                RecommendedRailCollector().onLaunch(
                    scope = scopeFor(stateFlow),
                    dependencies = mockk<LibraryDependencies>(relaxed = true),
                )
            }

            // ----- Act -----
            val rails = stateFlow.value.recommendedRailByTab

            // ----- Assert -----
            rails.keys shouldBe setOf(wantToReadTab.id)
            job.cancel()
        }

        @Test
        fun `removes the rail when the shelf grows to five books`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val stateFlow = MutableStateFlow(state(shelfBooks = shelf(count = 4)))
            val job = launch {
                RecommendedRailCollector().onLaunch(
                    scope = scopeFor(stateFlow),
                    dependencies = mockk<LibraryDependencies>(relaxed = true),
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(
                booksByTab = persistentMapOf(wantToReadTab.id to shelf(count = 5)),
            )

            // ----- Assert -----
            stateFlow.value.recommendedRailByTab.keys.shouldBeEmpty()
            job.cancel()
        }
    }
}
