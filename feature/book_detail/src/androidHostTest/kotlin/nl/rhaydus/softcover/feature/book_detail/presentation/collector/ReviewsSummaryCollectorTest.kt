package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ReviewsSummaryCollectorTest {
    private lateinit var dependencies: BookDetailDependencies
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        stateFlow = MutableStateFlow(BookDetailUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(BookDetailLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
        dependencies = mockk(relaxed = true)
    }

    private fun TestScope.launchCollector() {
        backgroundScope.launch {
            ReviewsSummaryCollector().onLaunch(
                scope = scope,
                dependencies = dependencies,
            )
        }
    }

    private fun stubBook(
        rating: Double,
        ratingsCount: Int,
    ): Book = mockk {
        every {
            this@mockk.rating
        } returns rating

        every {
            this@mockk.ratingsCount
        } returns ratingsCount
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `leaves the summary null while the book has not loaded`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Act -----
            launchCollector()

            // ----- Assert -----
            stateFlow.value.reviewsSummary shouldBe null
        }

        @Test
        fun `sets the summary once the book arrives`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            launchCollector()

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(
                book = stubBook(
                    rating = 4.2,
                    ratingsCount = 87,
                ),
            )

            // ----- Assert -----
            stateFlow.value.reviewsSummary shouldBe "4.2 across 87 ratings"
        }

        @Test
        fun `updates the summary when the ratings change`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = stateFlow.value.copy(
                book = stubBook(
                    rating = 4.2,
                    ratingsCount = 87,
                ),
            )
            launchCollector()

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(
                book = stubBook(
                    rating = 3.5,
                    ratingsCount = 90,
                ),
            )

            // ----- Assert -----
            stateFlow.value.reviewsSummary shouldBe "3.5 across 90 ratings"
        }

        @Test
        fun `clears the summary when the book goes away`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = stateFlow.value.copy(
                book = stubBook(
                    rating = 4.2,
                    ratingsCount = 87,
                ),
            )
            launchCollector()

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(book = null)

            // ----- Assert -----
            stateFlow.value.reviewsSummary shouldBe null
        }

        @Test
        fun `leaves the summary null for a book with no ratings`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            launchCollector()

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(
                book = stubBook(
                    rating = 0.0,
                    ratingsCount = 0,
                ),
            )

            // ----- Assert -----
            stateFlow.value.reviewsSummary shouldBe null
        }
    }
}
