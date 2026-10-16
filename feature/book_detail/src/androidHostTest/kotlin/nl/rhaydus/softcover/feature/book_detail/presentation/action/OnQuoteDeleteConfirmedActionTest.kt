package nl.rhaydus.softcover.feature.book_detail.presentation.action

import io.kotest.matchers.nulls.shouldBeNull
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
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.designsystem.util.SnackBarManager
import nl.rhaydus.softcover.core.personal.domain.usecase.DeleteQuoteUseCase
import nl.rhaydus.softcover.feature.book_detail.presentation.buildBookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnQuoteDeleteConfirmedActionTest {
    private lateinit var deleteQuoteUseCase: DeleteQuoteUseCase
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var eventChannel: Channel<BookDetailEvent>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        deleteQuoteUseCase = mockk()
        stateFlow = MutableStateFlow(buildBookDetailUiState())
        eventChannel = Channel(Channel.BUFFERED)
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(BookDetailLocalVariables()),
            eventChannel = eventChannel,
        )

        mockkObject(SnackBarManager)
        every {
            SnackBarManager.showSnackbar(title = any())
        } returns Unit
        coEvery {
            deleteQuoteUseCase(localId = any())
        } returns Result.success(Unit)
    }

    @AfterEach
    fun tearDown() {
        unmockkObject(SnackBarManager)
    }

    private fun stubDependencies(testScope: TestScope): BookDetailDependencies {
        val dispatcher = UnconfinedTestDispatcher(testScope.testScheduler)

        return mockk<BookDetailDependencies>(relaxed = true).also { mock ->
            every {
                mock.deleteQuoteUseCase
            } returns deleteQuoteUseCase
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
        fun `deletes the requested quote and clears the request`() = runTest {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(quoteDeleteRequestId = 5L)
            val action = OnQuoteDeleteConfirmedAction()

            // ----- Act -----
            action.execute(
                dependencies = stubDependencies(this),
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                deleteQuoteUseCase(localId = 5L)
            }
            stateFlow.value.quoteDeleteRequestId.shouldBeNull()
        }

        @Test
        fun `deletes nothing when no delete was requested`() = runTest {
            // ----- Arrange -----
            val action = OnQuoteDeleteConfirmedAction()

            // ----- Act -----
            action.execute(
                dependencies = stubDependencies(this),
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 0) {
                deleteQuoteUseCase(localId = any())
            }
        }

        @Test
        fun `shows a failure message and sends MutationFailedEvent when the delete fails`() = runTest {
            // ----- Arrange -----
            coEvery {
                deleteQuoteUseCase(localId = any())
            } returns Result.failure(RuntimeException("delete failed"))
            stateFlow.value = buildBookDetailUiState().copy(quoteDeleteRequestId = 5L)
            val action = OnQuoteDeleteConfirmedAction()

            // ----- Act -----
            action.execute(
                dependencies = stubDependencies(this),
                scope = scope,
            )

            // ----- Assert -----
            verify(exactly = 1) {
                SnackBarManager.showSnackbar(title = "Couldn't delete your quote. Try again.")
            }
            eventChannel.tryReceive().getOrNull().shouldBeInstanceOf<BookDetailEvent.MutationFailedEvent>()
        }
    }
}
