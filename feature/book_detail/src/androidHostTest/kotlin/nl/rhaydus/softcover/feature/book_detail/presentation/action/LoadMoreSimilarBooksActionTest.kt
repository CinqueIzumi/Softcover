package nl.rhaydus.softcover.feature.book_detail.presentation.action

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.book.domain.model.SimilarBooksPage
import nl.rhaydus.softcover.core.book.domain.usecase.LoadSimilarBooksPageUseCase
import nl.rhaydus.softcover.feature.book_detail.presentation.buildBookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.buildSimilarBook
import nl.rhaydus.softcover.feature.book_detail.presentation.buildSimilarBooksPage
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class LoadMoreSimilarBooksActionTest {
    private lateinit var loadSimilarBooksPageUseCase: LoadSimilarBooksPageUseCase
    private lateinit var dependencies: BookDetailDependencies
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var localVariablesFlow: MutableStateFlow<BookDetailLocalVariables>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        loadSimilarBooksPageUseCase = mockk()
        stateFlow = MutableStateFlow(buildBookDetailUiState())
        localVariablesFlow = MutableStateFlow(BookDetailLocalVariables(similarBooksSourceId = SOURCE_ID))
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = localVariablesFlow,
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    private fun stubDependencies(testScope: TestScope): BookDetailDependencies {
        val dispatcher = UnconfinedTestDispatcher(testScope.testScheduler)
        return mockk<BookDetailDependencies>(relaxed = true).also { mock ->
            every {
                mock.loadSimilarBooksPageUseCase
            } returns loadSimilarBooksPageUseCase

            every {
                mock.coroutineScope
            } returns testScope

            every {
                mock.mainDispatcher
            } returns dispatcher

            every {
                mock.launch(any())
            } answers { callOriginal() }
        }
    }

    @Nested
    inner class Execute {
        @Test
        fun `does nothing while a page load is already running`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)
            val runningJob = Job()
            localVariablesFlow.value = localVariablesFlow.value.copy(similarBooksLoadJob = runningJob)

            // ----- Act -----
            LoadMoreSimilarBooksAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 0) {
                loadSimilarBooksPageUseCase(
                    sourceBookId = any(),
                    offset = any(),
                    pageSize = any(),
                )
            }
            stateFlow.value.isLoadingSimilarBooks shouldBe false
            runningJob.cancel()
        }

        @Test
        fun `does nothing when the ranking has no more pages`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)
            stateFlow.value = stateFlow.value.copy(similarBooksHasMore = false)

            // ----- Act -----
            LoadMoreSimilarBooksAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 0) {
                loadSimilarBooksPageUseCase(
                    sourceBookId = any(),
                    offset = any(),
                    pageSize = any(),
                )
            }
            stateFlow.value.isLoadingSimilarBooks shouldBe false
        }

        @Test
        fun `does nothing before a source book has been recorded`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)
            localVariablesFlow.value = BookDetailLocalVariables()

            // ----- Act -----
            LoadMoreSimilarBooksAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 0) {
                loadSimilarBooksPageUseCase(
                    sourceBookId = any(),
                    offset = any(),
                    pageSize = any(),
                )
            }
        }

        @Test
        fun `stores the first page, advances the offset and settles the flags`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)
            coEvery {
                loadSimilarBooksPageUseCase(
                    sourceBookId = SOURCE_ID,
                    offset = 0,
                )
            } returns Result.success(
                buildSimilarBooksPage(
                    ids = listOf(10, 11),
                    nextOffset = 2,
                    hasMore = true,
                ),
            )

            // ----- Act -----
            LoadMoreSimilarBooksAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            localVariablesFlow.value.similarBooks.map { it.id } shouldBe listOf(10, 11)
            localVariablesFlow.value.similarBooksOffset shouldBe 2
            stateFlow.value.similarBookIds shouldBe listOf(10, 11)
            stateFlow.value.similarBooksHasMore shouldBe true
            stateFlow.value.isLoadingSimilarBooks shouldBe false
        }

        @Test
        fun `requests the next page from the stored offset and appends after the loaded books`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)
            localVariablesFlow.value = localVariablesFlow.value.copy(
                similarBooks = listOf(buildSimilarBook(id = 10), buildSimilarBook(id = 11)),
                similarBooksOffset = 2,
            )
            coEvery {
                loadSimilarBooksPageUseCase(
                    sourceBookId = SOURCE_ID,
                    offset = 2,
                )
            } returns Result.success(
                buildSimilarBooksPage(
                    ids = listOf(12),
                    nextOffset = 3,
                    hasMore = false,
                ),
            )

            // ----- Act -----
            LoadMoreSimilarBooksAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.similarBookIds shouldBe listOf(10, 11, 12)
            localVariablesFlow.value.similarBooksOffset shouldBe 3
            stateFlow.value.similarBooksHasMore shouldBe false
        }

        @Test
        fun `drops books of a new page that are already loaded`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)
            localVariablesFlow.value = localVariablesFlow.value.copy(
                similarBooks = listOf(buildSimilarBook(id = 10), buildSimilarBook(id = 11)),
                similarBooksOffset = 2,
            )
            coEvery {
                loadSimilarBooksPageUseCase(
                    sourceBookId = SOURCE_ID,
                    offset = 2,
                )
            } returns Result.success(
                buildSimilarBooksPage(
                    ids = listOf(11, 12),
                    nextOffset = 4,
                    hasMore = true,
                ),
            )

            // ----- Act -----
            LoadMoreSimilarBooksAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            localVariablesFlow.value.similarBooks.map { it.id } shouldBe listOf(10, 11, 12)
            stateFlow.value.similarBookIds shouldBe listOf(10, 11, 12)
        }

        @Test
        fun `drops a page book that is another edition of a loaded book`() = runTest {
            // ----- Arrange -----
            val loadedBook = buildSimilarBook(
                id = 10,
                canonicalId = 50,
            )
            val otherEdition = buildSimilarBook(
                id = 11,
                canonicalId = 50,
            )
            dependencies = stubDependencies(this)
            localVariablesFlow.value = localVariablesFlow.value.copy(
                similarBooks = listOf(loadedBook),
                similarBooksOffset = 1,
            )
            coEvery {
                loadSimilarBooksPageUseCase(
                    sourceBookId = SOURCE_ID,
                    offset = 1,
                )
            } returns Result.success(
                SimilarBooksPage(
                    books = listOf(otherEdition),
                    nextOffset = 2,
                    hasMore = false,
                ),
            )

            // ----- Act -----
            LoadMoreSimilarBooksAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.similarBookIds shouldBe listOf(10)
        }

        @Test
        fun `drops a page book whose id is the canonical id of a loaded book`() = runTest {
            // ----- Arrange -----
            val loadedBook = buildSimilarBook(
                id = 10,
                canonicalId = 50,
            )
            val canonicalBook = buildSimilarBook(id = 50)
            dependencies = stubDependencies(this)
            localVariablesFlow.value = localVariablesFlow.value.copy(
                similarBooks = listOf(loadedBook),
                similarBooksOffset = 1,
            )
            coEvery {
                loadSimilarBooksPageUseCase(
                    sourceBookId = SOURCE_ID,
                    offset = 1,
                )
            } returns Result.success(
                SimilarBooksPage(
                    books = listOf(canonicalBook),
                    nextOffset = 2,
                    hasMore = false,
                ),
            )

            // ----- Act -----
            LoadMoreSimilarBooksAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.similarBookIds shouldBe listOf(10)
        }

        @Test
        fun `requests again from the advanced offset when the first page is filtered empty`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)
            coEvery {
                loadSimilarBooksPageUseCase(
                    sourceBookId = SOURCE_ID,
                    offset = 0,
                )
            } returns Result.success(
                buildSimilarBooksPage(
                    ids = emptyList(),
                    nextOffset = 20,
                    hasMore = true,
                ),
            )
            coEvery {
                loadSimilarBooksPageUseCase(
                    sourceBookId = SOURCE_ID,
                    offset = 20,
                )
            } returns Result.success(
                buildSimilarBooksPage(
                    ids = listOf(30),
                    nextOffset = 40,
                    hasMore = true,
                ),
            )

            // ----- Act -----
            LoadMoreSimilarBooksAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.similarBookIds shouldBe listOf(30)
            localVariablesFlow.value.similarBooksOffset shouldBe 40
            stateFlow.value.isLoadingSimilarBooks shouldBe false
        }

        @Test
        fun `does not chase an empty page when the ranking is spent`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)
            coEvery {
                loadSimilarBooksPageUseCase(
                    sourceBookId = SOURCE_ID,
                    offset = 0,
                )
            } returns Result.success(
                buildSimilarBooksPage(
                    ids = emptyList(),
                    nextOffset = 5,
                    hasMore = false,
                ),
            )

            // ----- Act -----
            LoadMoreSimilarBooksAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                loadSimilarBooksPageUseCase(
                    sourceBookId = any(),
                    offset = any(),
                    pageSize = any(),
                )
            }
            stateFlow.value.similarBookIds shouldBe emptyList()
            stateFlow.value.similarBooksHasMore shouldBe false
        }

        @Test
        fun `stops chasing empty pages after a bounded number of requests`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)
            coEvery {
                loadSimilarBooksPageUseCase(
                    sourceBookId = SOURCE_ID,
                    offset = any(),
                    pageSize = any(),
                )
            } answers {
                val offset = secondArg<Int>()
                Result.success(
                    SimilarBooksPage(
                        books = emptyList(),
                        nextOffset = offset + 20,
                        hasMore = true,
                    ),
                )
            }

            // ----- Act -----
            LoadMoreSimilarBooksAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 3) {
                loadSimilarBooksPageUseCase(
                    sourceBookId = any(),
                    offset = any(),
                    pageSize = any(),
                )
            }
            stateFlow.value.similarBookIds shouldBe emptyList()
            stateFlow.value.similarBooksHasMore shouldBe true
            stateFlow.value.isLoadingSimilarBooks shouldBe false
        }

        @Test
        fun `collapses the section when the first page fails`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)
            coEvery {
                loadSimilarBooksPageUseCase(
                    sourceBookId = SOURCE_ID,
                    offset = 0,
                )
            } returns Result.failure(RuntimeException("network error"))

            // ----- Act -----
            LoadMoreSimilarBooksAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.similarBookIds.shouldNotBeNull() shouldBe emptyList()
            stateFlow.value.similarBooksHasMore shouldBe false
            stateFlow.value.isLoadingSimilarBooks shouldBe false
        }

        @Test
        fun `keeps the loaded cards and stops paging when a later page fails`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)
            localVariablesFlow.value = localVariablesFlow.value.copy(
                similarBooks = listOf(buildSimilarBook(id = 10), buildSimilarBook(id = 11)),
                similarBooksOffset = 2,
            )
            coEvery {
                loadSimilarBooksPageUseCase(
                    sourceBookId = SOURCE_ID,
                    offset = 2,
                )
            } returns Result.failure(RuntimeException("network error"))

            // ----- Act -----
            LoadMoreSimilarBooksAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.similarBookIds shouldBe listOf(10, 11)
            stateFlow.value.similarBooksHasMore shouldBe false
            stateFlow.value.isLoadingSimilarBooks shouldBe false
        }
    }

    private companion object {
        const val SOURCE_ID = 5
    }
}
