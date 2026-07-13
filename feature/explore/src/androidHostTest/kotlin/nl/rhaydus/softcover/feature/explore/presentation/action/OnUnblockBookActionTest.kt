package nl.rhaydus.softcover.feature.explore.presentation.action

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkObject
import io.mockk.verify
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.designsystem.util.SnackBarManager
import nl.rhaydus.softcover.feature.explore.domain.model.DismissedSeriesBook
import nl.rhaydus.softcover.feature.explore.domain.usecase.DismissContinueSeriesBookUseCase
import nl.rhaydus.softcover.feature.explore.domain.usecase.UndoContinueSeriesBookDismissalUseCase
import nl.rhaydus.softcover.feature.explore.presentation.event.HiddenSuggestionsEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.HiddenSuggestionsDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.HiddenSuggestionsLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.HiddenSuggestionsUiState
import nl.rhaydus.toad.ActionScope

class OnUnblockBookActionTest {
    private lateinit var undoContinueSeriesBookDismissalUseCase: UndoContinueSeriesBookDismissalUseCase
    private lateinit var dismissContinueSeriesBookUseCase: DismissContinueSeriesBookUseCase
    private lateinit var stateFlow: MutableStateFlow<HiddenSuggestionsUiState>
    private lateinit var scope: ActionScope<HiddenSuggestionsUiState, HiddenSuggestionsEvent, HiddenSuggestionsLocalVariables>

    @BeforeEach
    fun setUp() {
        undoContinueSeriesBookDismissalUseCase = mockk()
        dismissContinueSeriesBookUseCase = mockk(relaxed = true)
        stateFlow = MutableStateFlow(HiddenSuggestionsUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(HiddenSuggestionsLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        mockkObject(SnackBarManager)

        // `mockkObject` delegates unstubbed calls to the real singleton (it behaves like a spy, not
        // a strict mock), and the real implementation launches on `Dispatchers.Main`, which is unset
        // in this JVM host test. Stub every call by default so success-path tests that don't assert
        // on the snackbar itself don't crash; individual tests override this where they need to
        // inspect or trigger the passed lambda.
        every {
            SnackBarManager.showSnackBar(
                title = any(),
                actionLabel = any(),
                duration = any(),
                onActionClick = any(),
                onDismiss = any(),
            )
        } returns Unit
    }

    @AfterEach
    fun tearDown() {
        unmockkObject(SnackBarManager)
    }

    private fun stubDependencies(testScope: TestScope): HiddenSuggestionsDependencies {
        val dispatcher = UnconfinedTestDispatcher(testScope.testScheduler)
        return mockk<HiddenSuggestionsDependencies>(relaxed = true).also { mock ->
            every {
                mock.undoContinueSeriesBookDismissalUseCase
            } returns undoContinueSeriesBookDismissalUseCase

            every {
                mock.dismissContinueSeriesBookUseCase
            } returns dismissContinueSeriesBookUseCase

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

    @Nested
    inner class Execute {
        @Test
        fun `invokes undoContinueSeriesBookDismissalUseCase with the given bookId`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)

            coEvery {
                undoContinueSeriesBookDismissalUseCase(bookId = 42)
            } returns Result.success(Unit)

            val book = DismissedSeriesBook(
                bookId = 42,
                title = "Dune",
                coverUrl = "cover.jpg",
                authorText = "Frank Herbert",
                seriesName = "Dune Saga",
                seriesId = 99,
                seriesPosition = 3.0,
            )
            val action = OnUnblockBookAction(book = book)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                undoContinueSeriesBookDismissalUseCase(bookId = 42)
            }
        }

        @Test
        fun `shows an Undo snackbar with the book title on success`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)

            coEvery {
                undoContinueSeriesBookDismissalUseCase(bookId = 42)
            } returns Result.success(Unit)

            val book = DismissedSeriesBook(
                bookId = 42,
                title = "Dune",
                coverUrl = "cover.jpg",
                authorText = "Frank Herbert",
                seriesName = "Dune Saga",
                seriesId = 99,
                seriesPosition = 3.0,
            )
            val action = OnUnblockBookAction(book = book)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            verify {
                SnackBarManager.showSnackBar(
                    title = "\"Dune\" unblocked",
                    actionLabel = "Undo",
                    duration = any(),
                    onActionClick = any(),
                    onDismiss = any(),
                )
            }
        }

        @Test
        fun `falls back to a generic label when title is null`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)

            coEvery {
                undoContinueSeriesBookDismissalUseCase(bookId = 7)
            } returns Result.success(Unit)

            val book = DismissedSeriesBook(
                bookId = 7,
                title = null,
                coverUrl = null,
                authorText = null,
                seriesName = null,
                seriesId = null,
                seriesPosition = null,
            )
            val action = OnUnblockBookAction(book = book)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            verify {
                SnackBarManager.showSnackBar(
                    title = "\"Book\" unblocked",
                    actionLabel = "Undo",
                    duration = any(),
                    onActionClick = any(),
                    onDismiss = any(),
                )
            }
        }

        @Test
        fun `does not show a snackbar when the use case fails`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)

            coEvery {
                undoContinueSeriesBookDismissalUseCase(bookId = 42)
            } returns Result.failure(RuntimeException("db error"))

            val book = DismissedSeriesBook(
                bookId = 42,
                title = "Dune",
                coverUrl = "cover.jpg",
                authorText = "Frank Herbert",
                seriesName = "Dune Saga",
                seriesId = 99,
                seriesPosition = 3.0,
            )
            val action = OnUnblockBookAction(book = book)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            verify(exactly = 0) {
                SnackBarManager.showSnackBar(
                    title = any(),
                    actionLabel = any(),
                    duration = any(),
                    onActionClick = any(),
                    onDismiss = any(),
                )
            }
        }

        @Test
        fun `clicking Undo re-dismisses the book with seriesId and seriesPosition intact`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)
            val onActionClickSlot = slot<() -> Unit>()

            coEvery {
                undoContinueSeriesBookDismissalUseCase(bookId = 42)
            } returns Result.success(Unit)

            every {
                SnackBarManager.showSnackBar(
                    title = any(),
                    actionLabel = any(),
                    duration = any(),
                    onActionClick = capture(onActionClickSlot),
                    onDismiss = any(),
                )
            } returns Unit

            val book = DismissedSeriesBook(
                bookId = 42,
                title = "Dune",
                coverUrl = "cover.jpg",
                authorText = "Frank Herbert",
                seriesName = "Dune Saga",
                seriesId = 99,
                seriesPosition = 3.0,
            )

            coEvery {
                dismissContinueSeriesBookUseCase(book = book)
            } returns Result.success(Unit)

            val action = OnUnblockBookAction(book = book)

            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Act -----
            onActionClickSlot.captured.invoke()

            // ----- Assert -----
            coVerify {
                dismissContinueSeriesBookUseCase(book = book)
            }
        }
    }
}
