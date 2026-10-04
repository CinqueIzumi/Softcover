package nl.rhaydus.softcover.feature.book_detail.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.richtext.RichTextUiModel
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.model.BookReviewUiModel
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnRevealReviewSpoilerActionTest {
    private lateinit var dependencies: BookDetailDependencies
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var localVariablesFlow: MutableStateFlow<BookDetailLocalVariables>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        dependencies = mockk(relaxed = true)
        stateFlow = MutableStateFlow(BookDetailUiState())
        localVariablesFlow = MutableStateFlow(BookDetailLocalVariables())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = localVariablesFlow,
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    private fun review(
        id: Int,
        spoilerRevealed: Boolean = false,
    ) = BookReviewUiModel(
        id = id,
        body = RichTextUiModel.EMPTY,
        hasSpoilers = true,
        spoilerRevealed = spoilerRevealed,
        rating = null,
        reviewedMonthYear = null,
        reviewerName = "Alice",
        reviewerAvatarUrl = null,
        isLoading = false,
    )

    @Nested
    inner class Execute {
        @Test
        fun `reveals only the review with the matching id`() = runTest {
            // ----- Arrange -----
            stateFlow.value = BookDetailUiState(
                reviews = persistentListOf(review(id = 1), review(id = 2), review(id = 3)),
            )
            val action = OnRevealReviewSpoilerAction(reviewId = 2)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.reviews.map { it.spoilerRevealed } shouldBe listOf(false, true, false)
        }

        @Test
        fun `keeps an already revealed review revealed`() = runTest {
            // ----- Arrange -----
            val revealed = review(
                id = 1,
                spoilerRevealed = true,
            )
            val unrevealed = review(id = 2)
            stateFlow.value = BookDetailUiState(
                reviews = persistentListOf(
                    revealed,
                    unrevealed,
                ),
            )
            val action = OnRevealReviewSpoilerAction(reviewId = 2)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.reviews.map { it.spoilerRevealed } shouldBe listOf(true, true)
        }

        @Test
        fun `leaves the reviews untouched when no id matches`() = runTest {
            // ----- Arrange -----
            val reviews = persistentListOf(review(id = 1), review(id = 2))
            stateFlow.value = BookDetailUiState(reviews = reviews)
            val action = OnRevealReviewSpoilerAction(reviewId = 99)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.reviews shouldBe reviews
        }

        @Test
        fun `changes nothing but the matching review`() = runTest {
            // ----- Arrange -----
            stateFlow.value = BookDetailUiState(reviews = persistentListOf(review(id = 5)))
            val action = OnRevealReviewSpoilerAction(reviewId = 5)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            val expected = review(
                id = 5,
                spoilerRevealed = true,
            )
            stateFlow.value.reviews shouldBe listOf(expected)
        }
    }
}
