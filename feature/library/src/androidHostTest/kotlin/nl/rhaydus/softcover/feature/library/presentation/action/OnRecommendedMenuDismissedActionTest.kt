package nl.rhaydus.softcover.feature.library.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.designsystem.util.SnackBarManager
import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.core.domain.model.Author
import nl.rhaydus.softcover.core.lists.domain.model.HiddenRecommendation
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.openRecommendedMenuSession
import nl.rhaydus.softcover.feature.library.presentation.recommendedBook
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.softcover.feature.library.presentation.state.PendingRecommendedHide
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnRecommendedMenuDismissedActionTest {
    private lateinit var dependencies: LibraryDependencies
    private lateinit var stateFlow: MutableStateFlow<LibraryUiState>
    private lateinit var scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>

    @BeforeEach
    fun setUp() {
        dependencies = mockk(relaxed = true)
        stateFlow = MutableStateFlow(LibraryUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LibraryLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        mockkObject(SnackBarManager)
        every {
            SnackBarManager.showSnackBar(
                title = any(),
                actionLabel = any(),
                duration = any(),
                onActionClick = any(),
                onDismiss = any(),
            )
        } returns Unit
        coEvery {
            dependencies.hideRecommendedBookUseCase(book = any())
        } returns Result.success(Unit)
    }

    @AfterEach
    fun tearDown() {
        unmockkObject(SnackBarManager)
    }

    @Nested
    inner class Execute {
        @Test
        fun `hides the book when Not for me was chosen and clears the sheet`() = runTest {
            // ----- Arrange -----
            val author = Author(
                id = 1,
                name = "Frank Herbert",
            )
            val book = recommendedBook(
                id = 7,
                title = "Dune",
                authors = listOf(author),
            )
            stateFlow.value = LibraryUiState(
                recommendedBooks = persistentListOf(book),
                recommendedMenu = openRecommendedMenuSession(
                    book = book,
                    pending = PendingRecommendedHide(bookId = 7),
                ),
                recommendedMenuBookId = 7,
            )

            // ----- Act -----
            OnRecommendedMenuDismissedAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                dependencies.hideRecommendedBookUseCase(
                    book = HiddenRecommendation(
                        bookId = 7,
                        title = "Dune",
                        coverUrl = "cover-7.jpg",
                        authorText = "Frank Herbert",
                    ),
                )
            }
            stateFlow.value.recommendedMenu shouldBe SheetSession.Closed
            stateFlow.value.recommendedMenuBookId shouldBe null
        }

        @Test
        fun `hides nothing and clears the sheet when it was dismissed without a choice`() = runTest {
            // ----- Arrange -----
            val book = recommendedBook(id = 7)
            stateFlow.value = LibraryUiState(
                recommendedBooks = persistentListOf(book),
                recommendedMenu = openRecommendedMenuSession(book = book),
                recommendedMenuBookId = 7,
            )

            // ----- Act -----
            OnRecommendedMenuDismissedAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 0) {
                dependencies.hideRecommendedBookUseCase(book = any())
            }
            stateFlow.value.recommendedMenu shouldBe SheetSession.Closed
            stateFlow.value.recommendedMenuBookId shouldBe null
        }

        @Test
        fun `hides nothing when the chosen book has left the rail`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(
                recommendedBooks = persistentListOf(recommendedBook(id = 8)),
                recommendedMenu = openRecommendedMenuSession(
                    book = recommendedBook(id = 7),
                    pending = PendingRecommendedHide(bookId = 7),
                ),
            )

            // ----- Act -----
            OnRecommendedMenuDismissedAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 0) {
                dependencies.hideRecommendedBookUseCase(book = any())
            }
            stateFlow.value.recommendedMenu shouldBe SheetSession.Closed
        }
    }
}
