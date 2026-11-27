package nl.rhaydus.softcover.feature.book_detail.presentation.action

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
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
import nl.rhaydus.softcover.core.component.haptics.Feedback
import nl.rhaydus.softcover.core.corpus.domain.usecase.SaveDidNotFinishReasonsUseCase
import nl.rhaydus.softcover.core.domain.model.DidNotFinishReason
import nl.rhaydus.softcover.core.domain.model.DidNotFinishReasons
import nl.rhaydus.softcover.feature.book_detail.presentation.buildBookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.buildDidNotFinishReasonDraft
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnSaveDidNotFinishReasonsActionTest {
    private lateinit var saveDidNotFinishReasonsUseCase: SaveDidNotFinishReasonsUseCase
    private val savedReasons = mutableListOf<DidNotFinishReasons>()
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var eventChannel: Channel<BookDetailEvent>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        savedReasons.clear()
        saveDidNotFinishReasonsUseCase = mockk()

        coEvery {
            saveDidNotFinishReasonsUseCase(reasons = any())
        } answers {
            savedReasons += firstArg<DidNotFinishReasons>()
            Result.success(Unit)
        }

        stateFlow = MutableStateFlow(buildBookDetailUiState(bookId = 42))
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
    }

    @AfterEach
    fun tearDown() {
        unmockkObject(SnackBarManager)
    }

    private fun stubDependencies(testScope: TestScope): BookDetailDependencies {
        val dispatcher = UnconfinedTestDispatcher(testScope.testScheduler)

        return mockk<BookDetailDependencies>(relaxed = true).also { mock ->
            every {
                mock.saveDidNotFinishReasonsUseCase
            } returns saveDidNotFinishReasonsUseCase

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
        fun `saves the drafted reasons and own words for the book and clears the draft`() = runTest {
            // ----- Arrange -----
            stateFlow.value = stateFlow.value.copy(
                didNotFinishReasonDraft = buildDidNotFinishReasonDraft(
                    reasons = setOf(DidNotFinishReason.TooSlow, DidNotFinishReason.LostInterest),
                    ownWords = "The middle dragged",
                ),
            )

            // ----- Act -----
            OnSaveDidNotFinishReasonsAction().execute(
                dependencies = stubDependencies(this),
                scope = scope,
            )

            // ----- Assert -----
            savedReasons shouldBe listOf(
                DidNotFinishReasons(
                    bookId = 42,
                    reasons = setOf(DidNotFinishReason.TooSlow, DidNotFinishReason.LostInterest),
                    ownWords = "The middle dragged",
                ),
            )

            stateFlow.value.didNotFinishReasonDraft.shouldBeNull()
        }

        @Test
        fun `sends a routine MutationSucceededEvent after a successful save`() = runTest {
            // ----- Arrange -----
            stateFlow.value = stateFlow.value.copy(didNotFinishReasonDraft = buildDidNotFinishReasonDraft())

            // ----- Act -----
            OnSaveDidNotFinishReasonsAction().execute(
                dependencies = stubDependencies(this),
                scope = scope,
            )

            // ----- Assert -----
            val event = eventChannel.tryReceive().getOrNull()
                .shouldBeInstanceOf<BookDetailEvent.MutationSucceededEvent>()

            event.feedback shouldBe Feedback.Routine
        }

        @Test
        fun `shows a message and sends MutationFailedEvent when the save fails`() = runTest {
            // ----- Arrange -----
            coEvery {
                saveDidNotFinishReasonsUseCase(reasons = any())
            } returns Result.failure(RuntimeException("save failed"))

            stateFlow.value = stateFlow.value.copy(didNotFinishReasonDraft = buildDidNotFinishReasonDraft())

            // ----- Act -----
            OnSaveDidNotFinishReasonsAction().execute(
                dependencies = stubDependencies(this),
                scope = scope,
            )

            // ----- Assert -----
            verify(exactly = 1) {
                SnackBarManager.showSnackbar(title = "Couldn't save your reasons. Try again.")
            }

            eventChannel.tryReceive().getOrNull().shouldBeInstanceOf<BookDetailEvent.MutationFailedEvent>()
        }

        @Test
        fun `does nothing when there is no draft`() = runTest {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState(bookId = 42)

            // ----- Act -----
            OnSaveDidNotFinishReasonsAction().execute(
                dependencies = stubDependencies(this),
                scope = scope,
            )

            // ----- Assert -----
            savedReasons.shouldBeEmpty()
            eventChannel.tryReceive().getOrNull().shouldBeNull()
        }
    }
}
