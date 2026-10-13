package nl.rhaydus.softcover.feature.book_detail.presentation.action

import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.ReviewDocument
import nl.rhaydus.softcover.core.domain.model.ReviewParagraph
import nl.rhaydus.softcover.core.domain.model.ReviewRun
import nl.rhaydus.softcover.feature.book_detail.domain.model.BookReview
import nl.rhaydus.softcover.feature.book_detail.domain.model.BookReviewer
import nl.rhaydus.softcover.feature.book_detail.domain.usecase.GetTopBookReviewsUseCase
import nl.rhaydus.softcover.feature.book_detail.presentation.buildBookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.mapper.toBookReviewUiModel
import nl.rhaydus.softcover.feature.book_detail.presentation.model.BookReviewUiModel
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope
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
        stateFlow = MutableStateFlow(buildBookDetailUiState())
        localVariablesFlow = MutableStateFlow(BookDetailLocalVariables())
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
            paragraphs = listOf(
                ReviewParagraph(
                    runs = listOf(ReviewRun(text = "A great read")),
                ),
            ),
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
        fun `shows the stand-ins while the use case is still running`() = runTest {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(reviews = persistentListOf())
            dependencies = stubDependencies(this)

            var reviewsAtInvocation: List<BookReviewUiModel>? = null
            coEvery {
                getTopBookReviewsUseCase(bookId = 42)
            } coAnswers {
                reviewsAtInvocation = stateFlow.value.reviews
                Result.success(emptyList())
            }

            val action = FetchBookReviewsAction(bookId = 42)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            reviewsAtInvocation.shouldNotBeNull().shouldNotBeEmpty()
            reviewsAtInvocation.shouldNotBeNull().all { it.isLoading } shouldBe true
        }

        @Test
        fun `replaces the stand-ins with the mapped reviews on success`() = runTest {
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
            stateFlow.value.reviews shouldBe expectedReviews.map { it.toBookReviewUiModel() }
            stateFlow.value.reviews.none { it.isLoading } shouldBe true
        }

        @Test
        fun `ends with no reviews when the use case fails`() = runTest {
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
        }
    }
}
