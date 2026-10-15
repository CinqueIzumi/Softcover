package nl.rhaydus.softcover.feature.book_detail.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.book.domain.usecase.GetEditionsByBookIdUseCase
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.book_detail.presentation.buildBookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.buildEdition
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnScannedEditionReturnedActionTest {
    private lateinit var getEditionsByBookIdUseCase: GetEditionsByBookIdUseCase
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>
    private lateinit var localVariablesFlow: MutableStateFlow<BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        getEditionsByBookIdUseCase = mockk()
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
                mock.getEditionsByBookIdUseCase
            } returns getEditionsByBookIdUseCase

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

    private fun stubBook(id: Int = 1): Book = mockk<Book>().also {
        every {
            it.id
        } returns id
    }

    @Nested
    inner class Execute {
        @Test
        fun `pins the scanned edition, resets the banner and clears the preview`() = runTest {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(
                book = stubBook(id = 9),
                scannedEditionId = 1,
                scannedEditionBannerDismissed = true,
                previewEdition = buildEdition(id = 3),
            )
            val dependencies = stubDependencies(this)

            coEvery {
                getEditionsByBookIdUseCase(
                    bookId = 9,
                    forceNetwork = true,
                )
            } returns Result.success(emptyList())

            // ----- Act -----
            OnScannedEditionReturnedAction(editionId = 77).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.scannedEditionId shouldBe 77
            stateFlow.value.scannedEditionBannerDismissed shouldBe false
            stateFlow.value.previewEdition shouldBe null
        }

        @Test
        fun `refetches past the cache and writes editions without touching the book`() = runTest {
            // ----- Arrange -----
            val book = stubBook(id = 9)
            val fetched = listOf(buildEdition(id = 77))
            stateFlow.value = buildBookDetailUiState().copy(book = book)
            val dependencies = stubDependencies(this)

            coEvery {
                getEditionsByBookIdUseCase(
                    bookId = 9,
                    forceNetwork = true,
                )
            } returns Result.success(fetched)

            // ----- Act -----
            OnScannedEditionReturnedAction(editionId = 77).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.editions shouldBe fetched
            stateFlow.value.book shouldBe book
            localVariablesFlow.value.editionsLoadedForBookId shouldBe 9
        }

        @Test
        fun `leaves editions untouched and unmarked when the refetch fails`() = runTest {
            // ----- Arrange -----
            val existing = persistentListOf(buildEdition(id = 5))
            stateFlow.value = buildBookDetailUiState().copy(
                book = stubBook(id = 9),
                editions = existing,
            )
            val dependencies = stubDependencies(this)

            coEvery {
                getEditionsByBookIdUseCase(
                    bookId = 9,
                    forceNetwork = true,
                )
            } returns Result.failure(RuntimeException("offline"))

            // ----- Act -----
            OnScannedEditionReturnedAction(editionId = 77).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.editions shouldBe existing
            stateFlow.value.scannedEditionId shouldBe 77
            localVariablesFlow.value.editionsLoadedForBookId shouldBe null
        }

        @Test
        fun `does nothing when no book is loaded`() = runTest {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(book = null)
            val dependencies = stubDependencies(this)

            // ----- Act -----
            OnScannedEditionReturnedAction(editionId = 77).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.scannedEditionId shouldBe null
            coVerify(exactly = 0) {
                getEditionsByBookIdUseCase(
                    bookId = any(),
                    forceNetwork = any(),
                )
            }
        }
    }
}
