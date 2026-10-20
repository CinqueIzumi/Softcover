package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.book.domain.usecase.ObserveShelvedBookIdsUseCase
import nl.rhaydus.softcover.core.component.bookcard.BookCardKey
import nl.rhaydus.softcover.core.component.bookcard.BookCardSet
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnSimilarBookClickAction
import nl.rhaydus.softcover.feature.book_detail.presentation.buildBookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.buildSimilarBook
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.mapper.SIMILAR_BOOKS_STAND_IN_CARDS
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SimilarBooksRailCollectorTest {
    private lateinit var observeShelvedBookIdsUseCase: ObserveShelvedBookIdsUseCase
    private lateinit var shelvedIdsFlow: MutableStateFlow<Set<Int>>
    private lateinit var dependencies: BookDetailDependencies
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var localVariablesFlow: MutableStateFlow<BookDetailLocalVariables>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        shelvedIdsFlow = MutableStateFlow(emptySet())
        observeShelvedBookIdsUseCase = mockk()
        every {
            observeShelvedBookIdsUseCase(ids = any())
        } returns shelvedIdsFlow
        dependencies = mockk(relaxed = true)
        every {
            dependencies.observeShelvedBookIdsUseCase
        } returns observeShelvedBookIdsUseCase
        stateFlow = MutableStateFlow(buildBookDetailUiState())
        localVariablesFlow = MutableStateFlow(BookDetailLocalVariables())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = localVariablesFlow,
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    private fun loadBooks(
        ids: List<Int>,
        isLoading: Boolean = false,
    ) {
        localVariablesFlow.value = BookDetailLocalVariables(similarBooks = ids.map { buildSimilarBook(id = it) })
        stateFlow.value = stateFlow.value.copy(
            book = buildSimilarBook(id = 5),
            similarBookIds = ids.toImmutableList(),
            isLoadingSimilarBooks = isLoading,
        )
    }

    private fun BookDetailUiState.railBookIds(): List<Int> = similarBooks.set.cards.map {
        it.key.shouldBeInstanceOf<BookCardKey.Book>().bookId
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `maps the loaded books to cards in rank order with click payloads`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                loadBooks(ids = listOf(30, 10, 20))
                val collector = SimilarBooksRailCollector()

                // ----- Act -----
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Assert -----
                stateFlow.value.railBookIds() shouldBe listOf(30, 10, 20)
                val payload = stateFlow.value.similarBooks.set[BookCardKey.Book(bookId = 10)]
                payload.shouldNotBeNull().shouldBeInstanceOf<OnSimilarBookClickAction>().bookId shouldBe 10
                job.cancel()
            }

        @Test
        fun `drops a book from the rail as soon as it is shelved`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            loadBooks(ids = listOf(10, 20, 30))
            val collector = SimilarBooksRailCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            shelvedIdsFlow.value = setOf(20)

            // ----- Assert -----
            stateFlow.value.railBookIds() shouldBe listOf(10, 30)
            job.cancel()
        }

        @Test
        fun `leaves the stand-ins in place until the first page has settled`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = stateFlow.value.copy(
                book = buildSimilarBook(id = 5),
                similarBookIds = null,
                isLoadingSimilarBooks = true,
            )
            val collector = SimilarBooksRailCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            stateFlow.value.similarBooks.set shouldBe BookCardSet.standIns(SIMILAR_BOOKS_STAND_IN_CARDS)
            job.cancel()
        }

        @Test
        fun `collapses to an empty rail when loaded with no books`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = stateFlow.value.copy(
                book = buildSimilarBook(id = 5),
                similarBookIds = persistentListOf(),
                isLoadingSimilarBooks = false,
            )
            val collector = SimilarBooksRailCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            stateFlow.value.similarBooks.set.isEmpty shouldBe true
            job.cancel()
        }

        @Test
        fun `flags the rail as appending while a further page loads over existing cards`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                loadBooks(
                    ids = listOf(10, 20),
                    isLoading = true,
                )
                val collector = SimilarBooksRailCollector()

                // ----- Act -----
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Assert -----
                stateFlow.value.similarBooks.isAppending shouldBe true
                job.cancel()
            }

        @Test
        fun `lets the rail load more only while the ranking has more pages`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            loadBooks(ids = listOf(10, 20))
            stateFlow.value = stateFlow.value.copy(similarBooksHasMore = true)
            val collector = SimilarBooksRailCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }
            val whileMore = stateFlow.value.similarBooks.canLoadMore

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(similarBooksHasMore = false)

            // ----- Assert -----
            whileMore shouldBe true
            stateFlow.value.similarBooks.canLoadMore shouldBe false
            job.cancel()
        }

        @Test
        fun `does not flag the rail as appending once loading finishes`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            loadBooks(
                ids = listOf(10, 20),
                isLoading = true,
            )
            val collector = SimilarBooksRailCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(isLoadingSimilarBooks = false)

            // ----- Assert -----
            stateFlow.value.similarBooks.isAppending shouldBe false
            job.cancel()
        }
    }
}
