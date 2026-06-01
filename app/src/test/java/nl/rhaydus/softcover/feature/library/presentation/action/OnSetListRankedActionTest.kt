package nl.rhaydus.softcover.feature.library.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.LibrarySortMode
import nl.rhaydus.softcover.core.domain.model.SortDirection
import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.core.presentation.util.SnackBarManager
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.softcover.core.lists.domain.usecase.SetListRankedUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.SetLibrarySortUseCase
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnSetListRankedActionTest {

    private lateinit var setListRankedUseCase: SetListRankedUseCase
    private lateinit var setLibrarySortUseCase: SetLibrarySortUseCase
    private lateinit var stateFlow: MutableStateFlow<LibraryUiState>
    private lateinit var scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>

    private val listId = 7
    private val listName = "Favourites"
    private val tabId = "list-$listId"

    @BeforeEach
    fun setUp() {
        setListRankedUseCase = mockk(relaxed = true)
        setLibrarySortUseCase = mockk(relaxed = true)
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
            every { mock.setListRankedUseCase } returns setListRankedUseCase
            every { mock.setLibrarySortUseCase } returns setLibrarySortUseCase
        }

    @Nested
    inner class Execute {

        @Test
        fun `ranked=true happy path — sort switched to ORDER before ranked use case, dropdown closed, no snackbar`() =
            runTest {
                // ----- Arrange -----
                stateFlow.value = LibraryUiState(
                    isSortMenuExpanded = true,
                    customLists = listOf(
                        BookList(
                            id = listId,
                            name = listName,
                            slug = "favourites",
                            books = emptyList(),
                        ),
                    ),
                )

                coEvery {
                    setListRankedUseCase(
                        listId = any(),
                        ranked = any(),
                    )
                } returns Result.success(Unit)

                val action = OnSetListRankedAction(
                    listId = listId,
                    ranked = true,
                )

                // ----- Act -----
                action.execute(
                    dependencies = stubDependencies(),
                    scope = scope,
                )

                // ----- Assert -----
                stateFlow.value.isSortMenuExpanded shouldBe false

                coVerifyOrder {
                    setLibrarySortUseCase(
                        tabId = tabId,
                        mode = LibrarySortMode.ORDER,
                        direction = SortDirection.ASCENDING,
                    )

                    setListRankedUseCase(
                        listId = listId,
                        ranked = true,
                    )
                }

                verify(exactly = 0) { SnackBarManager.showSnackbar(title = any()) }
            }

        @Test
        fun `ranked=true failure path — sort rolls back to previous mode and direction, snackbar contains enable manual ordering and list name`() =
            runTest {
                // ----- Arrange -----
                val previousMode = LibrarySortMode.DATE_ADDED
                val previousDirection = SortDirection.DESCENDING

                stateFlow.value = LibraryUiState(
                    isSortMenuExpanded = true,
                    sortModeByTab = mapOf(tabId to previousMode),
                    sortDirectionByTab = mapOf(tabId to previousDirection),
                    customLists = listOf(
                        BookList(
                            id = listId,
                            name = listName,
                            slug = "favourites",
                            books = emptyList(),
                        ),
                    ),
                )

                coEvery {
                    setListRankedUseCase(
                        listId = any(),
                        ranked = any(),
                    )
                } returns Result.failure(RuntimeException("server error"))

                every { SnackBarManager.showSnackbar(title = any()) } returns Unit

                val action = OnSetListRankedAction(
                    listId = listId,
                    ranked = true,
                )

                // ----- Act -----
                action.execute(
                    dependencies = stubDependencies(),
                    scope = scope,
                )

                // ----- Assert -----
                coVerify {
                    setLibrarySortUseCase(
                        tabId = tabId,
                        mode = previousMode,
                        direction = previousDirection,
                    )
                }

                verify {
                    SnackBarManager.showSnackbar(
                        title = match { title ->
                            title.contains("enable manual ordering") && title.contains(listName)
                        },
                    )
                }
            }

        @Test
        fun `ranked=false happy path — no sort switch, only the ranked use case called`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(
                customLists = listOf(
                    BookList(
                        id = listId,
                        name = listName,
                        slug = "favourites",
                        books = emptyList(),
                    ),
                ),
            )

            coEvery {
                setListRankedUseCase(
                    listId = any(),
                    ranked = any(),
                )
            } returns Result.success(Unit)

            val action = OnSetListRankedAction(
                listId = listId,
                ranked = false,
            )

            // ----- Act -----
            action.execute(
                dependencies = stubDependencies(),
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                setListRankedUseCase(
                    listId = listId,
                    ranked = false,
                )
            }

            coVerify(exactly = 0) {
                setLibrarySortUseCase(
                    tabId = any(),
                    mode = any(),
                    direction = any(),
                )
            }
        }

        @Test
        fun `ranked=false failure path — snackbar contains disable manual ordering, no sort rollback`() =
            runTest {
                // ----- Arrange -----
                stateFlow.value = LibraryUiState(
                    customLists = listOf(
                        BookList(
                            id = listId,
                            name = listName,
                            slug = "favourites",
                            books = emptyList(),
                        ),
                    ),
                )

                coEvery {
                    setListRankedUseCase(
                        listId = any(),
                        ranked = any(),
                    )
                } returns Result.failure(RuntimeException("server error"))

                every { SnackBarManager.showSnackbar(title = any()) } returns Unit

                val action = OnSetListRankedAction(
                    listId = listId,
                    ranked = false,
                )

                // ----- Act -----
                action.execute(
                    dependencies = stubDependencies(),
                    scope = scope,
                )

                // ----- Assert -----
                verify {
                    SnackBarManager.showSnackbar(
                        title = match { it.contains("disable manual ordering") },
                    )
                }

                coVerify(exactly = 0) {
                    setLibrarySortUseCase(
                        tabId = any(),
                        mode = any(),
                        direction = any(),
                    )
                }
            }

        @Test
        fun `list missing from state — snackbar text lacks the for suffix`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(customLists = emptyList())

            coEvery {
                setListRankedUseCase(
                    listId = any(),
                    ranked = any(),
                )
            } returns Result.failure(RuntimeException("not found"))

            every { SnackBarManager.showSnackbar(title = any()) } returns Unit

            val action = OnSetListRankedAction(
                listId = listId,
                ranked = true,
            )

            // ----- Act -----
            action.execute(
                dependencies = stubDependencies(),
                scope = scope,
            )

            // ----- Assert -----
            verify {
                SnackBarManager.showSnackbar(
                    title = match { title ->
                        title.contains("enable manual ordering") && title.contains(" for \"").not()
                    },
                )
            }
        }
    }
}
