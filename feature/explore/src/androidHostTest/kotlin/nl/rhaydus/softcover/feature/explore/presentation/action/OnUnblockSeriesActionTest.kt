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
import nl.rhaydus.softcover.feature.explore.domain.usecase.DismissContinueSeriesUseCase
import nl.rhaydus.softcover.feature.explore.domain.usecase.UndoContinueSeriesDismissalUseCase
import nl.rhaydus.softcover.feature.explore.presentation.event.HiddenSuggestionsEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.HiddenSuggestionsDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.HiddenSuggestionsLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.HiddenSuggestionsUiState
import nl.rhaydus.toad.ActionScope

class OnUnblockSeriesActionTest {
    private lateinit var undoContinueSeriesDismissalUseCase: UndoContinueSeriesDismissalUseCase
    private lateinit var dismissContinueSeriesUseCase: DismissContinueSeriesUseCase
    private lateinit var stateFlow: MutableStateFlow<HiddenSuggestionsUiState>
    private lateinit var scope: ActionScope<HiddenSuggestionsUiState, HiddenSuggestionsEvent, HiddenSuggestionsLocalVariables>

    @BeforeEach
    fun setUp() {
        undoContinueSeriesDismissalUseCase = mockk()
        dismissContinueSeriesUseCase = mockk(relaxed = true)
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
                mock.undoContinueSeriesDismissalUseCase
            } returns undoContinueSeriesDismissalUseCase

            every {
                mock.dismissContinueSeriesUseCase
            } returns dismissContinueSeriesUseCase

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
        fun `invokes undoContinueSeriesDismissalUseCase with the given seriesId`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)

            coEvery {
                undoContinueSeriesDismissalUseCase(seriesId = 100)
            } returns Result.success(Unit)

            val action = OnUnblockSeriesAction(
                seriesId = 100,
                seriesName = "Foundation",
                coverUrl = "cover.jpg",
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                undoContinueSeriesDismissalUseCase(seriesId = 100)
            }
        }

        @Test
        fun `shows an Undo snackbar with the series name on success`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)

            coEvery {
                undoContinueSeriesDismissalUseCase(seriesId = 100)
            } returns Result.success(Unit)

            val action = OnUnblockSeriesAction(
                seriesId = 100,
                seriesName = "Foundation",
                coverUrl = "cover.jpg",
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            verify {
                SnackBarManager.showSnackBar(
                    title = "\"Foundation\" is back in your suggestions",
                    actionLabel = "Undo",
                    duration = any(),
                    onActionClick = any(),
                    onDismiss = any(),
                )
            }
        }

        @Test
        fun `falls back to a generic label when seriesName is null`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)

            coEvery {
                undoContinueSeriesDismissalUseCase(seriesId = 200)
            } returns Result.success(Unit)

            val action = OnUnblockSeriesAction(
                seriesId = 200,
                seriesName = null,
                coverUrl = null,
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            verify {
                SnackBarManager.showSnackBar(
                    title = "\"Series\" is back in your suggestions",
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
                undoContinueSeriesDismissalUseCase(seriesId = 100)
            } returns Result.failure(RuntimeException("db error"))

            val action = OnUnblockSeriesAction(
                seriesId = 100,
                seriesName = "Foundation",
                coverUrl = "cover.jpg",
            )

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
        fun `clicking Undo on the snackbar re-dismisses the series with the original metadata`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)
            val onActionClickSlot = slot<() -> Unit>()

            coEvery {
                undoContinueSeriesDismissalUseCase(seriesId = 100)
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

            coEvery {
                dismissContinueSeriesUseCase(
                    seriesId = 100,
                    seriesName = "Foundation",
                    coverUrl = "cover.jpg",
                )
            } returns Result.success(Unit)

            val action = OnUnblockSeriesAction(
                seriesId = 100,
                seriesName = "Foundation",
                coverUrl = "cover.jpg",
            )

            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Act -----
            onActionClickSlot.captured.invoke()

            // ----- Assert -----
            coVerify {
                dismissContinueSeriesUseCase(
                    seriesId = 100,
                    seriesName = "Foundation",
                    coverUrl = "cover.jpg",
                )
            }
        }

        @Test
        fun `clicking Undo re-dismisses the series with authorText and bookCount intact`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)
            val onActionClickSlot = slot<() -> Unit>()

            coEvery {
                undoContinueSeriesDismissalUseCase(seriesId = 100)
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

            coEvery {
                dismissContinueSeriesUseCase(
                    seriesId = 100,
                    seriesName = "Foundation",
                    coverUrl = "cover.jpg",
                    authorText = "Isaac Asimov",
                    bookCount = 7,
                )
            } returns Result.success(Unit)

            val action = OnUnblockSeriesAction(
                seriesId = 100,
                seriesName = "Foundation",
                coverUrl = "cover.jpg",
                authorText = "Isaac Asimov",
                bookCount = 7,
            )

            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Act -----
            onActionClickSlot.captured.invoke()

            // ----- Assert -----
            coVerify {
                dismissContinueSeriesUseCase(
                    seriesId = 100,
                    seriesName = "Foundation",
                    coverUrl = "cover.jpg",
                    authorText = "Isaac Asimov",
                    bookCount = 7,
                )
            }
        }
    }
}
