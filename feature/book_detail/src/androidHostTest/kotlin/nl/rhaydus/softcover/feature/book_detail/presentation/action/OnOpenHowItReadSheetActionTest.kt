package nl.rhaydus.softcover.feature.book_detail.presentation.action

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.coVerify
import io.mockk.verify
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.designsystem.util.SnackBarManager
import nl.rhaydus.softcover.core.corpus.domain.usecase.GetBookTraitsUseCase
import nl.rhaydus.softcover.core.domain.model.BookTraits
import nl.rhaydus.softcover.core.domain.model.Pace
import nl.rhaydus.softcover.feature.book_detail.presentation.buildBookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.buildQuoteBook
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnOpenHowItReadSheetActionTest {
    private lateinit var getBookTraitsUseCase: GetBookTraitsUseCase
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        getBookTraitsUseCase = mockk()
        stateFlow = MutableStateFlow(buildBookDetailUiState(bookId = 42))
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(BookDetailLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
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
                mock.getBookTraitsUseCase
            } returns getBookTraitsUseCase
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
        fun `seeds the draft from the stored traits of this book`() = runTest {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState(bookId = 42).copy(book = buildQuoteBook())
            coEvery {
                getBookTraitsUseCase(bookId = 42)
            } returns Result.success(
                BookTraits(
                    bookId = 42,
                    pace = Pace.FAST,
                    plotCharacterBalance = 50,
                ),
            )

            // ----- Act -----
            OnOpenHowItReadSheetAction().execute(
                dependencies = stubDependencies(this),
                scope = scope,
            )

            // ----- Assert -----
            val draft = stateFlow.value.howItReadDraft.shouldNotBeNull()
            draft.paceKey shouldBe "pace-fast"
            draft.balance shouldBe 0.5f
        }

        @Test
        fun `leaves the draft empty and shows a message when the traits cannot be read`() = runTest {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState(bookId = 42).copy(book = buildQuoteBook())
            coEvery {
                getBookTraitsUseCase(bookId = 42)
            } returns Result.failure(RuntimeException("read failed"))

            // ----- Act -----
            OnOpenHowItReadSheetAction().execute(
                dependencies = stubDependencies(this),
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.howItReadDraft.shouldBeNull()
            verify(exactly = 1) {
                SnackBarManager.showSnackbar(title = "Couldn't load your answers. Try again.")
            }
        }

        @Test
        fun `seeds no draft and reads nothing while the book has not loaded`() = runTest {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState(bookId = 42)

            // ----- Act -----
            OnOpenHowItReadSheetAction().execute(
                dependencies = stubDependencies(this),
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.howItReadDraft.shouldBeNull()
            coVerify(exactly = 0) {
                getBookTraitsUseCase(bookId = any())
            }
        }
    }
}
