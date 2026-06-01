package nl.rhaydus.softcover.feature.book_detail.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.ReviewDocument
import nl.rhaydus.softcover.core.domain.model.ReviewParagraph
import nl.rhaydus.softcover.core.domain.model.ReviewRun
import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.book_detail.domain.model.BookReview
import nl.rhaydus.softcover.feature.book_detail.domain.model.BookReviewer
import nl.rhaydus.softcover.feature.book_detail.domain.usecase.GetTopBookReviewsUseCase
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class FetchBookReviewsActionTest {

    private lateinit var getTopBookReviewsUseCase: GetTopBookReviewsUseCase
    private lateinit var dependencies: BookDetailDependencies
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var localVariablesFlow: MutableStateFlow<BookDetailLocalVariables>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        getTopBookReviewsUseCase = mockk()
        stateFlow = MutableStateFlow(BookDetailUiState())
        localVariablesFlow = MutableStateFlow(BookDetailLocalVariables())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = localVariablesFlow,
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    private fun stubDependencies(testScope: kotlinx.coroutines.test.TestScope): BookDetailDependencies {
        val dispatcher = UnconfinedTestDispatcher(testScope.testScheduler)
        return mockk<BookDetailDependencies>(relaxed = true).also { mock ->
            every {
                mock.getTopBookReviewsUseCase
            } returns getTopBookReviewsUseCase

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

    private fun stubReview(id: Int = 1): BookReview = BookReview(
        id = id,
        reviewDocument = ReviewDocument(
            paragraphs = listOf(ReviewParagraph(runs = listOf(ReviewRun(text = "A great read")))),
        ),
        hasSpoilers = false,
        rating = 4.0,
        reviewedAt = "2024-01-01",
        likesCount = 10,
        reviewer = BookReviewer(
            id = 5,
            username = "alice",
            name = "Alice",
            avatarUrl = null,
        ),
    )

    @Nested
    inner class Execute {

        @Test
        fun `sets loadingReviews to true and clears revealedSpoilerReviewIds before the use case is called`() = runTest {
            // ----- Arrange -----
            stateFlow.value = BookDetailUiState(
                loadingReviews = false,
                revealedSpoilerReviewIds = setOf(1, 2),
            )
            dependencies = stubDependencies(this)

            var loadingAtInvocation: Boolean? = null
            var revealedAtInvocation: Set<Int>? = null
            coEvery {
                getTopBookReviewsUseCase(bookId = 42)
            } coAnswers {
                loadingAtInvocation = stateFlow.value.loadingReviews
                revealedAtInvocation = stateFlow.value.revealedSpoilerReviewIds
                Result.success(emptyList())
            }

            val action = FetchBookReviewsAction(bookId = 42)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            loadingAtInvocation shouldBe true
            revealedAtInvocation shouldBe emptySet()
        }

        @Test
        fun `populates reviews and sets loadingReviews to false on success`() = runTest {
            // ----- Arrange -----
            val bookId = 42
            val expectedReviews = listOf(stubReview(id = 1), stubReview(id = 2))
            dependencies = stubDependencies(this)

            coEvery {
                getTopBookReviewsUseCase(bookId = bookId)
            } returns Result.success(expectedReviews)

            val action = FetchBookReviewsAction(bookId = bookId)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.reviews shouldBe expectedReviews
            stateFlow.value.loadingReviews shouldBe false
        }

        @Test
        fun `sets reviews to empty list and sets loadingReviews to false on failure`() = runTest {
            // ----- Arrange -----
            val bookId = 7
            dependencies = stubDependencies(this)

            coEvery {
                getTopBookReviewsUseCase(bookId = bookId)
            } returns Result.failure(RuntimeException("network error"))

            val action = FetchBookReviewsAction(bookId = bookId)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.reviews shouldBe emptyList()
            stateFlow.value.loadingReviews shouldBe false
        }

        @Test
        fun `invokes use case with the bookId provided to the action constructor`() = runTest {
            // ----- Arrange -----
            val bookId = 99
            dependencies = stubDependencies(this)

            coEvery {
                getTopBookReviewsUseCase(bookId = bookId)
            } returns Result.success(emptyList())

            val action = FetchBookReviewsAction(bookId = bookId)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                getTopBookReviewsUseCase(bookId = bookId)
            }
        }

        @Test
        fun `loadingReviews remains true while the use case is executing`() = runTest {
            // ----- Arrange -----
            val bookId = 5
            dependencies = stubDependencies(this)

            var loadingAtInvocation: Boolean? = null
            coEvery {
                getTopBookReviewsUseCase(bookId = bookId)
            } coAnswers {
                loadingAtInvocation = stateFlow.value.loadingReviews
                Result.success(emptyList())
            }

            val action = FetchBookReviewsAction(bookId = bookId)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            loadingAtInvocation shouldBe true
            stateFlow.value.loadingReviews shouldBe false
        }

        @Test
        fun `clears revealedSpoilerReviewIds even when they were previously populated`() = runTest {
            // ----- Arrange -----
            stateFlow.value = BookDetailUiState(revealedSpoilerReviewIds = setOf(10, 20, 30))
            val bookId = 42
            dependencies = stubDependencies(this)

            coEvery {
                getTopBookReviewsUseCase(bookId = bookId)
            } returns Result.success(emptyList())

            val action = FetchBookReviewsAction(bookId = bookId)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.revealedSpoilerReviewIds shouldBe emptySet()
        }
    }
}
