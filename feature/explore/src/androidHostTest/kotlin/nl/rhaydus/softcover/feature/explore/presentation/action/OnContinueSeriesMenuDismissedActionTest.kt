package nl.rhaydus.softcover.feature.explore.presentation.action

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.designsystem.util.SnackBarManager
import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.core.domain.model.Author
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookSeries
import nl.rhaydus.softcover.core.domain.preview.PreviewData
import nl.rhaydus.softcover.feature.explore.domain.model.DismissedSeriesBook
import nl.rhaydus.softcover.feature.explore.domain.usecase.DismissContinueSeriesBookUseCase
import nl.rhaydus.softcover.feature.explore.domain.usecase.DismissContinueSeriesUseCase
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.mapper.continueSeriesMenuSheetFor
import nl.rhaydus.softcover.feature.explore.presentation.model.ContinueSeriesDismissalKind
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.softcover.feature.explore.presentation.state.PendingContinueSeriesDismissal
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnContinueSeriesMenuDismissedActionTest {
    private lateinit var dismissBookUseCase: DismissContinueSeriesBookUseCase
    private lateinit var dismissSeriesUseCase: DismissContinueSeriesUseCase
    private lateinit var dependencies: ExploreDependencies
    private lateinit var stateFlow: MutableStateFlow<ExploreScreenUiState>
    private lateinit var scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>

    private val series = BookSeries(
        id = 4,
        name = "Earthsea",
        amountOfBooks = 6,
    )

    private val menu = continueSeriesMenuSheetFor(book = PreviewData.baseBook)

    @BeforeEach
    fun setUp() {
        dismissBookUseCase = mockk()
        dismissSeriesUseCase = mockk()
        dependencies = mockk(relaxed = true)
        every {
            dependencies.dismissContinueSeriesBookUseCase
        } returns dismissBookUseCase

        every {
            dependencies.dismissContinueSeriesUseCase
        } returns dismissSeriesUseCase

        coEvery {
            dismissBookUseCase(book = any())
        } returns Result.failure(IllegalStateException("stop"))

        coEvery {
            dismissSeriesUseCase(
                seriesId = any(),
                seriesName = any(),
                coverUrl = any(),
                authorText = any(),
                bookCount = any(),
            )
        } returns Result.failure(IllegalStateException("stop"))
        stateFlow = MutableStateFlow(ExploreScreenUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(ExploreLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
        mockkObject(SnackBarManager)
    }

    @AfterEach
    fun tearDown() {
        unmockkObject(SnackBarManager)
    }

    private fun book(
        bookSeries: BookSeries? = series,
        authors: List<Author> = emptyList(),
        positionsInSeries: List<Double> = listOf(2.0),
    ): Book = Book(
        id = 3,
        title = "The Tombs of Atuan",
        editions = emptyList(),
        defaultEdition = null,
        rating = 0.0,
        description = "",
        releaseYear = 1971,
        coverUrl = "https://covers.test/3.jpg",
        authors = authors,
        usersCount = 0,
        ratingsCount = 0,
        bookSeries = bookSeries,
        positionsInSeries = positionsInSeries,
        isCompilation = false,
        userBook = null,
    )

    private fun pend(
        bookId: Int,
        kind: ContinueSeriesDismissalKind,
        books: List<Book>,
    ) {
        stateFlow.value = stateFlow.value.copy(
            continueSeriesBooks = books.toImmutableList(),
            continueSeriesMenu = SheetSession.Open(
                model = menu.requestingClose(),
                pending = PendingContinueSeriesDismissal(
                    bookId = bookId,
                    kind = kind,
                ),
            ),
        )
    }

    @Nested
    inner class Execute {
        @Test
        fun `hides the book with its identity, series link and last series position`() = runTest {
            // ----- Arrange -----
            pend(
                bookId = 3,
                kind = ContinueSeriesDismissalKind.THIS_BOOK,
                books = listOf(
                    book(
                        authors = listOf(
                            Author(
                                id = 1,
                                name = "Ursula K. Le Guin",
                            ),
                        ),
                        positionsInSeries = listOf(2.0, 3.0),
                    ),
                ),
            )

            // ----- Act -----
            OnContinueSeriesMenuDismissedAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                dismissBookUseCase(
                    book = DismissedSeriesBook(
                        bookId = 3,
                        title = "The Tombs of Atuan",
                        coverUrl = "https://covers.test/3.jpg",
                        authorText = "Ursula K. Le Guin",
                        seriesName = "Earthsea",
                        seriesId = 4,
                        seriesPosition = 3.0,
                    ),
                )
            }
        }

        @Test
        fun `leaves the author text null when the book has no authors`() = runTest {
            // ----- Arrange -----
            pend(
                bookId = 3,
                kind = ContinueSeriesDismissalKind.THIS_BOOK,
                books = listOf(book(authors = emptyList())),
            )

            // ----- Act -----
            OnContinueSeriesMenuDismissedAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                dismissBookUseCase(book = match { it.authorText == null })
            }
        }

        @Test
        fun `hides a book without a series with no series link or position`() = runTest {
            // ----- Arrange -----
            pend(
                bookId = 3,
                kind = ContinueSeriesDismissalKind.THIS_BOOK,
                books = listOf(
                    book(
                        bookSeries = null,
                        positionsInSeries = emptyList(),
                    ),
                ),
            )

            // ----- Act -----
            OnContinueSeriesMenuDismissedAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                dismissBookUseCase(
                    book = match {
                        it.seriesId == null && it.seriesName == null && it.seriesPosition == null
                    },
                )
            }
        }

        @Test
        fun `hides the series with its name, cover, author text and book count`() = runTest {
            // ----- Arrange -----
            pend(
                bookId = 3,
                kind = ContinueSeriesDismissalKind.WHOLE_SERIES,
                books = listOf(
                    book(
                        authors = listOf(
                            Author(
                                id = 1,
                                name = "Ursula K. Le Guin",
                            ),
                        ),
                    ),
                ),
            )

            // ----- Act -----
            OnContinueSeriesMenuDismissedAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                dismissSeriesUseCase(
                    seriesId = 4,
                    seriesName = "Earthsea",
                    coverUrl = "https://covers.test/3.jpg",
                    authorText = "Ursula K. Le Guin",
                    bookCount = 6,
                )
            }
        }

        @Test
        fun `hides nothing for a whole-series choice when the book has no series`() = runTest {
            // ----- Arrange -----
            pend(
                bookId = 3,
                kind = ContinueSeriesDismissalKind.WHOLE_SERIES,
                books = listOf(book(bookSeries = null)),
            )

            // ----- Act -----
            OnContinueSeriesMenuDismissedAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 0) { dismissBookUseCase(book = any()) }
            coVerify(exactly = 0) {
                dismissSeriesUseCase(
                    seriesId = any(),
                    seriesName = any(),
                    coverUrl = any(),
                    authorText = any(),
                    bookCount = any(),
                )
            }
        }

        @Test
        fun `closes the session after resolving the pending dismissal`() = runTest {
            // ----- Arrange -----
            pend(
                bookId = 3,
                kind = ContinueSeriesDismissalKind.THIS_BOOK,
                books = listOf(book()),
            )

            // ----- Act -----
            OnContinueSeriesMenuDismissedAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.continueSeriesMenu shouldBe SheetSession.Closed
            stateFlow.value.continueSeriesMenuBookId.shouldBeNull()
        }

        @Test
        fun `closes the session and hides nothing when the sheet was swiped away without a choice`() = runTest {
            // ----- Arrange -----
            stateFlow.value = stateFlow.value.copy(
                continueSeriesBooks = persistentListOf(book()),
                continueSeriesMenu = SheetSession.Open(model = menu),
                continueSeriesMenuBookId = 3,
            )

            // ----- Act -----
            OnContinueSeriesMenuDismissedAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 0) { dismissBookUseCase(book = any()) }
            stateFlow.value.continueSeriesMenu shouldBe SheetSession.Closed
            stateFlow.value.continueSeriesMenuBookId.shouldBeNull()
        }

        @Test
        fun `does nothing when the session is already closed`() = runTest {
            // ----- Arrange -----
            stateFlow.value = stateFlow.value.copy(continueSeriesBooks = persistentListOf(book()))

            // ----- Act -----
            OnContinueSeriesMenuDismissedAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 0) { dismissBookUseCase(book = any()) }
            stateFlow.value.continueSeriesMenu shouldBe SheetSession.Closed
        }

        @Test
        fun `closes the session but hides nothing when the book is no longer listed`() = runTest {
            // ----- Arrange -----
            pend(
                bookId = 99,
                kind = ContinueSeriesDismissalKind.THIS_BOOK,
                books = listOf(book()),
            )

            // ----- Act -----
            OnContinueSeriesMenuDismissedAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 0) { dismissBookUseCase(book = any()) }
            stateFlow.value.continueSeriesMenu shouldBe SheetSession.Closed
        }
    }
}
