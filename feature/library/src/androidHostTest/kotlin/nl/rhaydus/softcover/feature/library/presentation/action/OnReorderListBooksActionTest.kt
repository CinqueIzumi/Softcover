package nl.rhaydus.softcover.feature.library.presentation.action

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.designsystem.util.SnackBarManager
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.lists.domain.usecase.ReorderListBooksUseCase
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnReorderListBooksActionTest {
    private lateinit var reorderListBooksUseCase: ReorderListBooksUseCase
    private lateinit var stateFlow: MutableStateFlow<LibraryUiState>
    private lateinit var scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>

    @BeforeEach
    fun setUp() {
        reorderListBooksUseCase = mockk(relaxed = true)
        stateFlow = MutableStateFlow(LibraryUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LibraryLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        mockkObject(SnackBarManager)
    }

    @AfterEach
    fun tearDown() {
        unmockkObject(SnackBarManager)
    }

    private fun stubDependencies(): LibraryDependencies =
        mockk<LibraryDependencies>(relaxed = true).also { mock ->
            every { mock.reorderListBooksUseCase } returns reorderListBooksUseCase
        }

    @Nested
    inner class Execute {
        @Test
        fun `invokes use case and shows no snackbar when result is success`() = runTest {
            // ----- Arrange -----
            val listId = 42
            val startPosition = 0
            val bookIds = listOf(10, 20, 30)
            val action = OnReorderListBooksAction(
                listId = listId,
                startPosition = startPosition,
                orderedListBookIds = bookIds,
            )

            coEvery {
                reorderListBooksUseCase(
                    listId = any(),
                    startPosition = any(),
                    orderedListBookIds = any(),
                )
            } returns Result.success(Unit)

            // ----- Act -----
            action.execute(
                dependencies = stubDependencies(),
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                reorderListBooksUseCase(
                    listId = listId,
                    startPosition = startPosition,
                    orderedListBookIds = bookIds,
                )
            }

            verify(exactly = 0) { SnackBarManager.showSnackbar(title = any()) }
        }

        @Test
        fun `does not invoke use case when orderedListBookIds is empty`() = runTest {
            // ----- Arrange -----
            val action = OnReorderListBooksAction(
                listId = 42,
                startPosition = 0,
                orderedListBookIds = emptyList(),
            )

            // ----- Act -----
            action.execute(
                dependencies = stubDependencies(),
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 0) {
                reorderListBooksUseCase(
                    listId = any(),
                    startPosition = any(),
                    orderedListBookIds = any(),
                )
            }
        }

        @Test
        fun `shows snackbar with list name when use case fails and list is in state`() = runTest {
            // ----- Arrange -----
            val listId = 7
            val listName = "Reading 2026"
            val action = OnReorderListBooksAction(
                listId = listId,
                startPosition = 0,
                orderedListBookIds = listOf(1, 2, 3),
            )

            stateFlow.value = LibraryUiState(
                customLists = listOf(
                    BookList(
                        id = listId,
                        name = listName,
                        slug = "reading-2026",
                        books = emptyList(),
                    ),
                ),
            )

            coEvery {
                reorderListBooksUseCase(
                    listId = any(),
                    startPosition = any(),
                    orderedListBookIds = any(),
                )
            } returns Result.failure(RuntimeException("network error"))

            every { SnackBarManager.showSnackbar(title = any()) } returns Unit

            // ----- Act -----
            action.execute(
                dependencies = stubDependencies(),
                scope = scope,
            )

            // ----- Assert -----
            verify {
                SnackBarManager.showSnackbar(title = match { it.contains(listName) })
            }
        }

        @Test
        fun `shows snackbar without list name when use case fails and list is absent from state`() =
            runTest {
                // ----- Arrange -----
                val action = OnReorderListBooksAction(
                    listId = 99,
                    startPosition = 0,
                    orderedListBookIds = listOf(5, 6),
                )

                coEvery {
                    reorderListBooksUseCase(
                        listId = any(),
                        startPosition = any(),
                        orderedListBookIds = any(),
                    )
                } returns Result.failure(RuntimeException("not found"))

                every { SnackBarManager.showSnackbar(title = any()) } returns Unit

                // ----- Act -----
                action.execute(
                    dependencies = stubDependencies(),
                    scope = scope,
                )

                // ----- Assert -----
                verify {
                    SnackBarManager.showSnackbar(
                        title = match { title ->
                            title.contains("Couldn't save the new order").and(
                                title.contains("in \"").not(),
                            )
                        },
                    )
                }
            }
    }
}
