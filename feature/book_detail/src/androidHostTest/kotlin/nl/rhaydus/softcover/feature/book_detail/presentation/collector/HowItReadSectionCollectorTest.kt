package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.traits.HowItReadBlockUiModel
import nl.rhaydus.softcover.core.corpus.domain.usecase.ObserveBookTraitsUseCase
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.core.domain.model.BookTraits
import nl.rhaydus.softcover.core.domain.model.Pace
import nl.rhaydus.softcover.feature.book_detail.presentation.TEST_BOOK_ID
import nl.rhaydus.softcover.feature.book_detail.presentation.buildBookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class HowItReadSectionCollectorTest {
    private lateinit var observeBookTraitsUseCase: ObserveBookTraitsUseCase
    private lateinit var storedTraits: MutableStateFlow<BookTraits>
    private lateinit var dependencies: BookDetailDependencies
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        storedTraits = MutableStateFlow(BookTraits(bookId = TEST_BOOK_ID))
        observeBookTraitsUseCase = mockk()
        every {
            observeBookTraitsUseCase(bookId = any())
        } returns storedTraits
        stateFlow = MutableStateFlow(buildBookDetailUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(BookDetailLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
        dependencies = mockk<BookDetailDependencies>(relaxed = true).also {
            every {
                it.observeBookTraitsUseCase
            } returns observeBookTraitsUseCase
        }
    }

    private fun bookWith(status: BookStatus): Book = mockk<Book>().also {
        every {
            it.status
        } returns status
    }

    private fun TestScope.launchCollector() {
        backgroundScope.launch {
            HowItReadSectionCollector().onLaunch(
                scope = scope,
                dependencies = dependencies,
            )
        }
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `shows the empty invitation for every started status while nothing is answered`() =
            runTest(UnconfinedTestDispatcher()) {
                val started = listOf(
                    BookStatus.Reading,
                    BookStatus.Paused,
                    BookStatus.Read,
                    BookStatus.DidNotFinish,
                )

                started.forEach { status ->
                    // ----- Arrange -----
                    stateFlow.value = buildBookDetailUiState().copy(book = bookWith(status = status))
                    storedTraits.value = BookTraits(bookId = TEST_BOOK_ID)

                    // ----- Act -----
                    launchCollector()

                    // ----- Assert -----
                    stateFlow.value.howItRead.shouldBeInstanceOf<HowItReadBlockUiModel.Empty>()
                }
            }

        @Test
        fun `shows the filled block for every started status once a trait is answered`() =
            runTest(UnconfinedTestDispatcher()) {
                val started = listOf(
                    BookStatus.Reading,
                    BookStatus.Paused,
                    BookStatus.Read,
                    BookStatus.DidNotFinish,
                )

                started.forEach { status ->
                    // ----- Arrange -----
                    stateFlow.value = buildBookDetailUiState().copy(book = bookWith(status = status))
                    storedTraits.value = BookTraits(
                        bookId = TEST_BOOK_ID,
                        pace = Pace.FAST,
                    )

                    // ----- Act -----
                    launchCollector()

                    // ----- Assert -----
                    val block = stateFlow.value.howItRead.shouldBeInstanceOf<HowItReadBlockUiModel.Filled>()
                    block.summary shouldBe "Fast pace"
                }
            }

        @Test
        fun `builds no section and never observes the traits for a book still to be read`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                stateFlow.value = buildBookDetailUiState().copy(book = bookWith(status = BookStatus.WantToRead))

                // ----- Act -----
                launchCollector()

                // ----- Assert -----
                stateFlow.value.howItRead.shouldBeNull()
                verify(exactly = 0) {
                    observeBookTraitsUseCase(bookId = any())
                }
            }

        @Test
        fun `builds no section when the book has not loaded`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState()

            // ----- Act -----
            launchCollector()

            // ----- Assert -----
            stateFlow.value.howItRead.shouldBeNull()
        }

        @Test
        fun `reveals the section when the status moves from want to read to reading`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                stateFlow.value = buildBookDetailUiState().copy(book = bookWith(status = BookStatus.WantToRead))
                launchCollector()

                // ----- Act -----
                stateFlow.value = stateFlow.value.copy(book = bookWith(status = BookStatus.Reading))

                // ----- Assert -----
                stateFlow.value.howItRead.shouldBeInstanceOf<HowItReadBlockUiModel.Empty>()
            }

        @Test
        fun `drops the section when the book is moved back to want to read`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(book = bookWith(status = BookStatus.Reading))
            launchCollector()

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(book = bookWith(status = BookStatus.WantToRead))

            // ----- Assert -----
            stateFlow.value.howItRead.shouldBeNull()
        }

        @Test
        fun `re-maps the block's expanded state when the section is toggled`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            storedTraits.value = BookTraits(
                bookId = TEST_BOOK_ID,
                pace = Pace.SLOW,
            )
            stateFlow.value = buildBookDetailUiState().copy(
                book = bookWith(status = BookStatus.Read),
                howItReadExpanded = false,
            )
            launchCollector()

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(howItReadExpanded = true)

            // ----- Assert -----
            val block = stateFlow.value.howItRead.shouldBeInstanceOf<HowItReadBlockUiModel.Filled>()
            block.expanded shouldBe true
        }

        @Test
        fun `updates the block when new traits are stored`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(book = bookWith(status = BookStatus.Read))
            launchCollector()

            // ----- Act -----
            storedTraits.value = BookTraits(
                bookId = TEST_BOOK_ID,
                pace = Pace.MODERATE,
            )

            // ----- Assert -----
            val block = stateFlow.value.howItRead.shouldBeInstanceOf<HowItReadBlockUiModel.Filled>()
            block.summary shouldBe "Moderate pace"
        }
    }
}
