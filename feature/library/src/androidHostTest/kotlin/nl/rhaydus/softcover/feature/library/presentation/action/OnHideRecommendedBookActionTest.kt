package nl.rhaydus.softcover.feature.library.presentation.action

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
import nl.rhaydus.designsystem.util.SnackBarManager
import nl.rhaydus.softcover.core.lists.domain.model.HiddenRecommendation
import nl.rhaydus.softcover.core.lists.domain.usecase.HideRecommendedBookUseCase
import nl.rhaydus.softcover.core.lists.domain.usecase.RestoreRecommendedBookUseCase
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnHideRecommendedBookActionTest {
    private lateinit var hideRecommendedBookUseCase: HideRecommendedBookUseCase
    private lateinit var restoreRecommendedBookUseCase: RestoreRecommendedBookUseCase
    private lateinit var scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>

    private val hidden = HiddenRecommendation(
        bookId = 7,
        title = "Dune",
        coverUrl = "cover.jpg",
        authorText = "Frank Herbert",
    )

    @BeforeEach
    fun setUp() {
        hideRecommendedBookUseCase = mockk()
        restoreRecommendedBookUseCase = mockk()
        scope = ActionScope(
            stateFlow = MutableStateFlow(LibraryUiState()),
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
    }

    @AfterEach
    fun tearDown() {
        unmockkObject(SnackBarManager)
    }

    private fun stubDependencies(testScope: TestScope): LibraryDependencies {
        val dispatcher = UnconfinedTestDispatcher(testScope.testScheduler)

        return mockk<LibraryDependencies>(relaxed = true).also { mock ->
            every {
                mock.hideRecommendedBookUseCase
            } returns hideRecommendedBookUseCase
            every {
                mock.restoreRecommendedBookUseCase
            } returns restoreRecommendedBookUseCase
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
        fun `hides the book and shows an Undo snackbar naming it`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)
            coEvery {
                hideRecommendedBookUseCase(book = hidden)
            } returns Result.success(Unit)

            // ----- Act -----
            OnHideRecommendedBookAction(book = hidden).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify { hideRecommendedBookUseCase(book = hidden) }
            verify {
                SnackBarManager.showSnackBar(
                    title = "\"Dune\" won't be suggested again",
                    actionLabel = "Undo",
                    duration = any(),
                    onActionClick = any(),
                    onDismiss = any(),
                )
            }
        }

        @Test
        fun `names the book generically when it has no title`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)
            val untitled = hidden.copy(title = null)
            coEvery {
                hideRecommendedBookUseCase(book = untitled)
            } returns Result.success(Unit)

            // ----- Act -----
            OnHideRecommendedBookAction(book = untitled).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            verify {
                SnackBarManager.showSnackBar(
                    title = "\"Book\" won't be suggested again",
                    actionLabel = any(),
                    duration = any(),
                    onActionClick = any(),
                    onDismiss = any(),
                )
            }
        }

        @Test
        fun `shows no snackbar when hiding fails`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)
            coEvery {
                hideRecommendedBookUseCase(book = hidden)
            } returns Result.failure(RuntimeException("boom"))

            // ----- Act -----
            OnHideRecommendedBookAction(book = hidden).execute(
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
        fun `clicking Undo restores the hidden book`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)
            val onActionClickSlot = slot<() -> Unit>()
            coEvery {
                hideRecommendedBookUseCase(book = hidden)
            } returns Result.success(Unit)
            coEvery {
                restoreRecommendedBookUseCase(bookId = 7)
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
            OnHideRecommendedBookAction(book = hidden).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Act -----
            onActionClickSlot.captured.invoke()

            // ----- Assert -----
            coVerify { restoreRecommendedBookUseCase(bookId = 7) }
        }
    }
}
