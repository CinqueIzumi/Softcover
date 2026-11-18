package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.corpus.domain.usecase.ObserveBookCopyUseCase
import nl.rhaydus.softcover.core.domain.model.AcquisitionKind
import nl.rhaydus.softcover.core.domain.model.AcquisitionSource
import nl.rhaydus.softcover.core.domain.model.BookCopy
import nl.rhaydus.softcover.feature.book_detail.presentation.TEST_BOOK_ID
import nl.rhaydus.softcover.feature.book_detail.presentation.buildBookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.buildQuoteBook
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.mapper.toYourCopyPanel
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class YourCopySectionCollectorTest {
    private lateinit var storedCopy: MutableStateFlow<BookCopy>
    private lateinit var dependencies: BookDetailDependencies
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        storedCopy = MutableStateFlow(BookCopy(bookId = TEST_BOOK_ID))

        val observeBookCopyUseCase = mockk<ObserveBookCopyUseCase>().also {
            every {
                it(bookId = any())
            } returns storedCopy
        }

        stateFlow = MutableStateFlow(buildBookDetailUiState())

        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(BookDetailLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        dependencies = mockk<BookDetailDependencies>(relaxed = true).also {
            every {
                it.observeBookCopyUseCase
            } returns observeBookCopyUseCase
        }
    }

    private fun TestScope.launchCollector() {
        backgroundScope.launch {
            YourCopySectionCollector().onLaunch(
                scope = scope,
                dependencies = dependencies,
            )
        }
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `builds no section for a book the reader has no copy of`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(book = buildQuoteBook(shelved = false))

            // ----- Act -----
            launchCollector()

            // ----- Assert -----
            stateFlow.value.yourCopy.shouldBeNull()
        }

        @Test
        fun `builds no section while the book has not loaded`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState()

            // ----- Act -----
            launchCollector()

            // ----- Assert -----
            stateFlow.value.yourCopy.shouldBeNull()
        }

        @Test
        fun `shows the stored copy once the book has a user copy`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val copy = BookCopy(
                bookId = TEST_BOOK_ID,
                source = AcquisitionSource.Preset(kind = AcquisitionKind.GIFT),
            )

            storedCopy.value = copy
            stateFlow.value = buildBookDetailUiState().copy(book = buildQuoteBook(shelved = true))

            // ----- Act -----
            launchCollector()

            // ----- Assert -----
            stateFlow.value.yourCopy shouldBe copy.toYourCopyPanel()
        }

        @Test
        fun `follows the stored copy as it changes`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(book = buildQuoteBook(shelved = true))
            launchCollector()

            val updated = BookCopy(
                bookId = TEST_BOOK_ID,
                source = AcquisitionSource.Other(text = "A street stall"),
            )

            // ----- Act -----
            storedCopy.value = updated

            // ----- Assert -----
            stateFlow.value.yourCopy shouldBe updated.toYourCopyPanel()
        }

        @Test
        fun `drops the section when the user copy goes away`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(book = buildQuoteBook(shelved = true))
            launchCollector()

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(book = buildQuoteBook(shelved = false))

            // ----- Assert -----
            stateFlow.value.yourCopy.shouldBeNull()
        }
    }
}
