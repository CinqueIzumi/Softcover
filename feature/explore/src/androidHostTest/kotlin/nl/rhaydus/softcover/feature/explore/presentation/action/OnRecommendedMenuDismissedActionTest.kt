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
import nl.rhaydus.softcover.core.domain.preview.PreviewData
import nl.rhaydus.softcover.core.lists.domain.model.HiddenRecommendation
import nl.rhaydus.softcover.core.lists.domain.usecase.HideRecommendedBookUseCase
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.mapper.recommendedMenuSheetFor
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.softcover.feature.explore.presentation.state.PendingRecommendedHide
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnRecommendedMenuDismissedActionTest {
    private lateinit var hideRecommendedBookUseCase: HideRecommendedBookUseCase
    private lateinit var dependencies: ExploreDependencies
    private lateinit var stateFlow: MutableStateFlow<ExploreScreenUiState>
    private lateinit var scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>

    private val eyebrow = "Recommended for you"

    @BeforeEach
    fun setUp() {
        hideRecommendedBookUseCase = mockk()
        dependencies = mockk(relaxed = true)
        every {
            dependencies.hideRecommendedBookUseCase
        } returns hideRecommendedBookUseCase

        // Failing keeps the follow-up snackbar out of the way; the tests only assert on the hide call.
        coEvery {
            hideRecommendedBookUseCase(book = any())
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

    private fun book(authors: List<Author> = emptyList()): Book = PreviewData.baseBook.copy(
        id = 3,
        title = "The Tombs of Atuan",
        coverUrl = "https://covers.test/3.jpg",
        editions = emptyList(),
        defaultEdition = null,
        userBook = null,
        authors = authors,
    )

    private fun pend(
        bookId: Int,
        books: List<Book>,
    ) {
        val menu = recommendedMenuSheetFor(
            book = PreviewData.baseBook,
            eyebrow = eyebrow,
        )

        stateFlow.value = stateFlow.value.copy(
            recommendedBooks = books.toImmutableList(),
            recommendedMenuBookId = bookId,
            recommendedMenu = SheetSession.Open(
                model = menu.requestingClose(),
                pending = PendingRecommendedHide(bookId = bookId),
            ),
        )
    }

    @Nested
    inner class Execute {
        @Test
        fun `hides the pending book with its title, cover and joined author names`() = runTest {
            // ----- Arrange -----
            val authors = listOf(
                Author(
                    id = 1,
                    name = "Ursula K. Le Guin",
                ),
                Author(
                    id = 2,
                    name = "Someone Else",
                ),
            )
            pend(
                bookId = 3,
                books = listOf(book(authors = authors)),
            )

            // ----- Act -----
            OnRecommendedMenuDismissedAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                hideRecommendedBookUseCase(
                    book = HiddenRecommendation(
                        bookId = 3,
                        title = "The Tombs of Atuan",
                        coverUrl = "https://covers.test/3.jpg",
                        authorText = "Ursula K. Le Guin, Someone Else",
                    ),
                )
            }
        }

        @Test
        fun `closes the sheet state after resolving the pending hide`() = runTest {
            // ----- Arrange -----
            pend(
                bookId = 3,
                books = listOf(book()),
            )

            // ----- Act -----
            OnRecommendedMenuDismissedAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.recommendedMenu shouldBe SheetSession.Closed
            stateFlow.value.recommendedMenuBookId.shouldBeNull()
        }

        @Test
        fun `closes the sheet state and hides nothing when it was swiped away without choosing`() = runTest {
            // ----- Arrange -----
            stateFlow.value = stateFlow.value.copy(
                recommendedBooks = persistentListOf(book()),
                recommendedMenuBookId = 3,
                recommendedMenu = SheetSession.Open(
                    model = recommendedMenuSheetFor(
                        book = book(),
                        eyebrow = eyebrow,
                    ),
                ),
            )

            // ----- Act -----
            OnRecommendedMenuDismissedAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 0) { hideRecommendedBookUseCase(book = any()) }
            stateFlow.value.recommendedMenu shouldBe SheetSession.Closed
            stateFlow.value.recommendedMenuBookId.shouldBeNull()
        }

        @Test
        fun `closes the sheet state but hides nothing when the pending book is no longer listed`() = runTest {
            // ----- Arrange -----
            pend(
                bookId = 99,
                books = listOf(book()),
            )

            // ----- Act -----
            OnRecommendedMenuDismissedAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 0) { hideRecommendedBookUseCase(book = any()) }
            stateFlow.value.recommendedMenu shouldBe SheetSession.Closed
        }
    }
}
