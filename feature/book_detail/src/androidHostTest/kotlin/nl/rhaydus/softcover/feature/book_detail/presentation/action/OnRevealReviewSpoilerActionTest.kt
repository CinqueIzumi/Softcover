package nl.rhaydus.softcover.feature.book_detail.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

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

    @Nested
    inner class Execute {
        @Test
        fun `adds the reviewId to revealedSpoilerReviewIds when the set was empty`() = runTest {
            // ----- Arrange -----
            val reviewId = 5
            stateFlow.value = BookDetailUiState(revealedSpoilerReviewIds = emptySet())
            val action = OnRevealReviewSpoilerAction(reviewId = reviewId)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.revealedSpoilerReviewIds shouldBe setOf(5)
        }

        @Test
        fun `adds the reviewId to revealedSpoilerReviewIds without removing existing ids`() = runTest {
            // ----- Arrange -----
            val existingId = 3
            val newId = 7
            stateFlow.value = BookDetailUiState(revealedSpoilerReviewIds = setOf(existingId))
            val action = OnRevealReviewSpoilerAction(reviewId = newId)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.revealedSpoilerReviewIds shouldBe setOf(existingId, newId)
        }

        @Test
        fun `does not duplicate a reviewId already present in the set`() = runTest {
            // ----- Arrange -----
            val reviewId = 11
            stateFlow.value = BookDetailUiState(revealedSpoilerReviewIds = setOf(reviewId))
            val action = OnRevealReviewSpoilerAction(reviewId = reviewId)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.revealedSpoilerReviewIds shouldBe setOf(reviewId)
        }

        @Test
        fun `preserves all previously revealed ids when adding a new one`() = runTest {
            // ----- Arrange -----
            val existing = setOf(1, 2, 3)
            val newId = 4
            stateFlow.value = BookDetailUiState(revealedSpoilerReviewIds = existing)
            val action = OnRevealReviewSpoilerAction(reviewId = newId)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.revealedSpoilerReviewIds shouldBe setOf(1, 2, 3, 4)
        }

        @Test
        fun `does not alter any other fields in the state`() = runTest {
            // ----- Arrange -----
            val initialState = BookDetailUiState(
                loadingReviews = true,
                revealedSpoilerReviewIds = emptySet(),
            )
            stateFlow.value = initialState
            val action = OnRevealReviewSpoilerAction(reviewId = 9)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.loadingReviews shouldBe true
        }
    }
}
