package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlin.time.Instant
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.PrivacySetting
import nl.rhaydus.softcover.core.domain.model.Quote
import nl.rhaydus.softcover.core.personal.domain.usecase.ObserveBookQuotesUseCase
import nl.rhaydus.softcover.feature.book_detail.presentation.buildBookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.buildQuoteBook
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class QuotesSectionCollectorTest {
    private lateinit var observeBookQuotesUseCase: ObserveBookQuotesUseCase
    private lateinit var storedQuotes: MutableStateFlow<List<Quote>>
    private lateinit var dependencies: BookDetailDependencies
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        storedQuotes = MutableStateFlow(emptyList())
        observeBookQuotesUseCase = mockk()
        every {
            observeBookQuotesUseCase(bookId = any())
        } returns storedQuotes
        stateFlow = MutableStateFlow(buildBookDetailUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(BookDetailLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
        dependencies = mockk<BookDetailDependencies>(relaxed = true).also {
            every {
                it.observeBookQuotesUseCase
            } returns observeBookQuotesUseCase
        }
    }

    private fun quote(localId: Long): Quote = Quote(
        localId = localId,
        remoteId = null,
        bookId = 7,
        editionId = null,
        text = "Quote $localId",
        privacy = PrivacySetting.PUBLIC,
        actionAt = Instant.parse("2020-03-05T12:00:00Z"),
        position = null,
        isPending = false,
    )

    private fun TestScope.launchCollector() {
        backgroundScope.launch {
            QuotesSectionCollector().onLaunch(
                scope = scope,
                dependencies = dependencies,
            )
        }
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `builds no section for a book that is not on a shelf`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(book = buildQuoteBook(shelved = false))

            // ----- Act -----
            launchCollector()

            // ----- Assert -----
            stateFlow.value.quotesSection.shouldBeNull()
        }

        @Test
        fun `keeps the section with its empty message while a shelved book has no quotes`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                stateFlow.value = buildBookDetailUiState().copy(book = buildQuoteBook())

                // ----- Act -----
                launchCollector()

                // ----- Assert -----
                val section = stateFlow.value.quotesSection.shouldNotBeNull()
                section.quotes.size shouldBe 0
                section.emptyMessage.shouldNotBeNull()
            }

        @Test
        fun `adds a card when a quote is stored after launch`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(book = buildQuoteBook())
            launchCollector()

            // ----- Act -----
            storedQuotes.value = listOf(quote(localId = 3))

            // ----- Assert -----
            val section = stateFlow.value.quotesSection.shouldNotBeNull()
            section.quotes.map { it.key } shouldBe listOf(3L)
            section.emptyMessage.shouldBeNull()
        }

        @Test
        fun `drops the section when the book leaves the shelf`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(book = buildQuoteBook())
            launchCollector()

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(book = buildQuoteBook(shelved = false))

            // ----- Assert -----
            stateFlow.value.quotesSection.shouldBeNull()
        }
    }
}
