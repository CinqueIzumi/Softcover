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
import nl.rhaydus.softcover.core.corpus.domain.usecase.SaveBookTraitsUseCase
import nl.rhaydus.softcover.core.domain.model.BookTraits
import nl.rhaydus.softcover.core.domain.model.Pace
import nl.rhaydus.softcover.feature.book_detail.presentation.buildBookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.state.HowItReadDraft
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnSaveBookTraitsActionTest {
    private lateinit var saveBookTraitsUseCase: SaveBookTraitsUseCase
    private val savedTraits = mutableListOf<BookTraits>()
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var eventChannel: Channel<BookDetailEvent>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        savedTraits.clear()
        saveBookTraitsUseCase = mockk()
        coEvery {
            saveBookTraitsUseCase(traits = any())
        } answers {
            savedTraits += firstArg<BookTraits>()
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
                mock.saveBookTraitsUseCase
            } returns saveBookTraitsUseCase
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
        fun `saves the draft's answers for this book and clears the draft`() = runTest {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState(bookId = 42).copy(
                howItReadDraft = HowItReadDraft(
                    paceKey = "pace-fast",
                    balance = 0.5f,
                ),
            )

            // ----- Act -----
            OnSaveBookTraitsAction().execute(
                dependencies = stubDependencies(this),
                scope = scope,
            )

            // ----- Assert -----
            savedTraits shouldBe listOf(
                BookTraits(
                    bookId = 42,
                    pace = Pace.FAST,
                    plotCharacterBalance = 50,
                ),
            )
            stateFlow.value.howItReadDraft.shouldBeNull()
        }

        @Test
        fun `saves an empty draft as empty traits so the stored answers are cleared`() = runTest {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState(bookId = 42).copy(howItReadDraft = HowItReadDraft())

            // ----- Act -----
            OnSaveBookTraitsAction().execute(
                dependencies = stubDependencies(this),
                scope = scope,
            )

            // ----- Assert -----
            savedTraits shouldBe listOf(BookTraits(bookId = 42))
        }

        @Test
        fun `does nothing when there is no draft`() = runTest {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState(bookId = 42)

            // ----- Act -----
            OnSaveBookTraitsAction().execute(
                dependencies = stubDependencies(this),
                scope = scope,
            )

            // ----- Assert -----
            savedTraits.shouldBeEmpty()
            eventChannel.tryReceive().getOrNull().shouldBeNull()
        }

        @Test
        fun `shows a message and sends MutationFailedEvent when the save fails`() = runTest {
            // ----- Arrange -----
            coEvery {
                saveBookTraitsUseCase(traits = any())
            } returns Result.failure(RuntimeException("save failed"))
            stateFlow.value = buildBookDetailUiState(bookId = 42).copy(howItReadDraft = HowItReadDraft())

            // ----- Act -----
            OnSaveBookTraitsAction().execute(
                dependencies = stubDependencies(this),
                scope = scope,
            )

            // ----- Assert -----
            verify(exactly = 1) {
                SnackBarManager.showSnackbar(title = "Couldn't save your answers. Try again.")
            }
            eventChannel.tryReceive().getOrNull().shouldBeInstanceOf<BookDetailEvent.MutationFailedEvent>()
        }
    }
}
