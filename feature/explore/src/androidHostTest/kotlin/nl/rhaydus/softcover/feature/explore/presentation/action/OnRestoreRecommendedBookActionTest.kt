package nl.rhaydus.softcover.feature.explore.presentation.action

import io.kotest.matchers.shouldBe
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
import nl.rhaydus.softcover.feature.explore.presentation.event.HiddenSuggestionsEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.HiddenSuggestionsDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.HiddenSuggestionsLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.HiddenSuggestionsUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnRestoreRecommendedBookActionTest {
    private lateinit var hideRecommendedBookUseCase: HideRecommendedBookUseCase
    private lateinit var restoreRecommendedBookUseCase: RestoreRecommendedBookUseCase
    private lateinit var scope: ActionScope<HiddenSuggestionsUiState, HiddenSuggestionsEvent, HiddenSuggestionsLocalVariables>

    private val book = HiddenRecommendation(
        bookId = 42,
        title = "Dune",
        coverUrl = "cover.jpg",
        authorText = "Frank Herbert",
    )

    @BeforeEach
    fun setUp() {
        hideRecommendedBookUseCase = mockk()
        restoreRecommendedBookUseCase = mockk()
        scope = ActionScope(
            stateFlow = MutableStateFlow(HiddenSuggestionsUiState()),
            localVariablesFlow = MutableStateFlow(HiddenSuggestionsLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        mockkObject(SnackBarManager)

        // The real SnackBarManager launches on `Dispatchers.Main`, which is unset on the JVM host.
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
        fun `restores the book and confirms with a snackbar naming its exact title`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)
            val titleSlot = slot<String>()

            coEvery {
                restoreRecommendedBookUseCase(bookId = 42)
            } returns Result.success(Unit)

            every {
                SnackBarManager.showSnackBar(
                    title = capture(titleSlot),
                    actionLabel = any(),
                    duration = any(),
                    onActionClick = any(),
                    onDismiss = any(),
                )
            } returns Unit

            // ----- Act -----
            OnRestoreRecommendedBookAction(book = book).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify { restoreRecommendedBookUseCase(bookId = 42) }
            titleSlot.captured shouldBe "\"Dune\" is back in your suggestions"
        }

        @Test
        fun `names the book generically when it has no title`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)
            val titleSlot = slot<String>()

            coEvery {
                restoreRecommendedBookUseCase(bookId = 42)
            } returns Result.success(Unit)

            every {
                SnackBarManager.showSnackBar(
                    title = capture(titleSlot),
                    actionLabel = any(),
                    duration = any(),
                    onActionClick = any(),
                    onDismiss = any(),
                )
            } returns Unit

            // ----- Act -----
            OnRestoreRecommendedBookAction(book = book.copy(title = null)).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            titleSlot.captured shouldBe "\"Book\" is back in your suggestions"
        }

        @Test
        fun `shows no snackbar when restoring fails`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)

            coEvery {
                restoreRecommendedBookUseCase(bookId = 42)
            } returns Result.failure(RuntimeException("db error"))

            // ----- Act -----
            OnRestoreRecommendedBookAction(book = book).execute(
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
        fun `clicking Undo hides the whole book again`() = runTest {
            // ----- Arrange -----
            val dependencies = stubDependencies(this)
            val onActionClickSlot = slot<() -> Unit>()

            coEvery {
                restoreRecommendedBookUseCase(bookId = 42)
            } returns Result.success(Unit)

            coEvery {
                hideRecommendedBookUseCase(book = book)
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

            OnRestoreRecommendedBookAction(book = book).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Act -----
            onActionClickSlot.captured.invoke()

            // ----- Assert -----
            coVerify { hideRecommendedBookUseCase(book = book) }
        }
    }
}
