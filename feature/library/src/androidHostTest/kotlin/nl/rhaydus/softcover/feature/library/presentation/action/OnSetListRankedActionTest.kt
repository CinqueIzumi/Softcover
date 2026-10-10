package nl.rhaydus.softcover.feature.library.presentation.action

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
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
import nl.rhaydus.softcover.core.domain.model.LibraryGridLayout
import nl.rhaydus.softcover.core.domain.model.LibrarySortMode
import nl.rhaydus.softcover.core.domain.model.SortDirection
import nl.rhaydus.softcover.core.lists.domain.usecase.SetListRankedUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.SetLibrarySortUseCase
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryArrangeDraft
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnSetListRankedActionTest {
    private lateinit var setListRankedUseCase: SetListRankedUseCase
    private lateinit var setLibrarySortUseCase: SetLibrarySortUseCase
    private lateinit var stateFlow: MutableStateFlow<LibraryUiState>
    private lateinit var eventChannel: Channel<LibraryEvent>
    private lateinit var scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>

    private val listId = 7
    private val listName = "Favourites"
    private val tabId = "list-$listId"

    @BeforeEach
    fun setUp() {
        setListRankedUseCase = mockk(relaxed = true)
        setLibrarySortUseCase = mockk(relaxed = true)
        stateFlow = MutableStateFlow(LibraryUiState())
        eventChannel = Channel(Channel.BUFFERED)
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LibraryLocalVariables()),
            eventChannel = eventChannel,
        )

        mockkObject(SnackBarManager)
    }

    @AfterEach
    fun tearDown() {
        unmockkObject(SnackBarManager)
    }

    private fun stubDependencies(): LibraryDependencies =
        mockk<LibraryDependencies>(relaxed = true).also { mock ->
            every {
                mock.setListRankedUseCase
            } returns setListRankedUseCase
            every {
                mock.setLibrarySortUseCase
            } returns setLibrarySortUseCase
        }

    @Nested
    inner class Execute {
        @Test
        fun `ranked=true switches the sort to ORDER, writes ranked and nudges the open draft, with no snackbar`() =
            runTest {
                // ----- Arrange -----
                val openDraft = LibraryArrangeDraft(
                    tabId = tabId,
                    sortMode = LibrarySortMode.DATE_ADDED,
                    sortDirection = SortDirection.DESCENDING,
                    gridLayout = LibraryGridLayout.GRID_TWO_COLUMNS,
                )
                stateFlow.value = LibraryUiState(
                    customLists = listOf(
                        BookList(
                            id = listId,
                            name = listName,
                            slug = "favourites",
                            books = emptyList(),
                        ),
                    ),
                    arrangeDraft = openDraft,
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
                coVerify(exactly = 1) {
                    setLibrarySortUseCase(
                        tabId = tabId,
                        mode = LibrarySortMode.ORDER,
                        direction = SortDirection.ASCENDING,
                    )
                }

                coVerify(exactly = 1) {
                    setListRankedUseCase(
                        listId = listId,
                        ranked = true,
                    )
                }

                stateFlow.value.arrangeDraft shouldBe openDraft.copy(
                    sortMode = LibrarySortMode.ORDER,
                    sortDirection = SortDirection.ASCENDING,
                )

                verify(exactly = 0) { SnackBarManager.showSnackbar(title = any()) }

            eventChannel.tryReceive().getOrNull() shouldBe null
            }

        @Test
        fun `ranked=true failure rolls the sort and the open draft back and names the list in the snackbar`() =
            runTest {
                // ----- Arrange -----
                val previousMode = LibrarySortMode.DATE_ADDED
                val previousDirection = SortDirection.DESCENDING
                val openDraft = LibraryArrangeDraft(
                    tabId = tabId,
                    sortMode = previousMode,
                    sortDirection = previousDirection,
                    gridLayout = LibraryGridLayout.GRID_TWO_COLUMNS,
                )

                stateFlow.value = LibraryUiState(
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
                    arrangeDraft = openDraft,
                )

                coEvery {
                    setListRankedUseCase(
                        listId = any(),
                        ranked = any(),
                    )
                } returns Result.failure(RuntimeException("server error"))

                every {
                    SnackBarManager.showSnackbar(title = any())
                } returns Unit

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

                stateFlow.value.arrangeDraft shouldBe openDraft

                verify {
                    SnackBarManager.showSnackbar(
                        title = match { title ->
                            title.contains("enable manual ordering") && title.contains(listName)
                        },
                    )
                }

                eventChannel.tryReceive().getOrNull().shouldBeInstanceOf<LibraryEvent.ListRankedChangeFailedEvent>()
                eventChannel.tryReceive().getOrNull() shouldBe null
            }

        @Test
        fun `ranked=false writes only the ranked flag and leaves the sort and the open draft alone`() = runTest {
            // ----- Arrange -----
            val openDraft = LibraryArrangeDraft(
                tabId = tabId,
                sortMode = LibrarySortMode.DATE_ADDED,
                sortDirection = SortDirection.DESCENDING,
                gridLayout = LibraryGridLayout.GRID_TWO_COLUMNS,
            )
            stateFlow.value = LibraryUiState(
                customLists = listOf(
                    BookList(
                        id = listId,
                        name = listName,
                        slug = "favourites",
                        books = emptyList(),
                    ),
                ),
                arrangeDraft = openDraft,
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

            stateFlow.value.arrangeDraft shouldBe openDraft

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

                every {
                    SnackBarManager.showSnackbar(title = any())
                } returns Unit

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

            every {
                SnackBarManager.showSnackbar(title = any())
            } returns Unit

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

        @Test
        fun `ranked=true with an open arrangeDraft for a different tab leaves the draft untouched`() = runTest {
            // ----- Arrange -----
            val otherTabDraft = LibraryArrangeDraft(
                tabId = "list-99",
                sortMode = LibrarySortMode.DATE_ADDED,
                sortDirection = SortDirection.DESCENDING,
                gridLayout = LibraryGridLayout.GRID_TWO_COLUMNS,
            )

            stateFlow.value = LibraryUiState(
                customLists = listOf(
                    BookList(
                        id = listId,
                        name = listName,
                        slug = "favourites",
                        books = emptyList(),
                    ),
                ),
                arrangeDraft = otherTabDraft,
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
            stateFlow.value.arrangeDraft shouldBe otherTabDraft
        }
    }
}
