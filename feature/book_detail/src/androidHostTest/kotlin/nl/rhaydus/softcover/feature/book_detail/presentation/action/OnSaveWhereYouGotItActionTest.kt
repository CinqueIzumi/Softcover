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
import nl.rhaydus.softcover.core.corpus.domain.usecase.GetBookCopyUseCase
import nl.rhaydus.softcover.core.corpus.domain.usecase.SaveBookCopyUseCase
import nl.rhaydus.softcover.core.domain.model.AcquisitionKind
import nl.rhaydus.softcover.core.domain.model.AcquisitionSource
import nl.rhaydus.softcover.core.domain.model.BookCopy
import nl.rhaydus.softcover.core.domain.model.PricePaid
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetCurrenciesUseCase
import nl.rhaydus.softcover.feature.book_detail.presentation.EURO
import nl.rhaydus.softcover.feature.book_detail.presentation.TEST_CURRENCIES
import nl.rhaydus.softcover.feature.book_detail.presentation.YEN
import nl.rhaydus.softcover.feature.book_detail.presentation.buildBookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.buildWhereYouGotItDraft
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.mapper.toKey
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.state.WhereYouGotItSource
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnSaveWhereYouGotItActionTest {
    private lateinit var getBookCopyUseCase: GetBookCopyUseCase
    private lateinit var saveBookCopyUseCase: SaveBookCopyUseCase
    private lateinit var getCurrenciesUseCase: GetCurrenciesUseCase
    private val savedCopies = mutableListOf<BookCopy>()
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var eventChannel: Channel<BookDetailEvent>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        savedCopies.clear()
        getBookCopyUseCase = mockk()
        saveBookCopyUseCase = mockk()

        getCurrenciesUseCase = mockk<GetCurrenciesUseCase>().also {
            every {
                it()
            } returns TEST_CURRENCIES
        }

        coEvery {
            getBookCopyUseCase(bookId = 42)
        } returns Result.success(
            BookCopy(
                bookId = 42,
                source = AcquisitionSource.Other(text = "Old"),
            ),
        )

        coEvery {
            saveBookCopyUseCase(copy = any())
        } answers {
            savedCopies += firstArg<BookCopy>()
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
                mock.getBookCopyUseCase
            } returns getBookCopyUseCase

            every {
                mock.saveBookCopyUseCase
            } returns saveBookCopyUseCase

            every {
                mock.getCurrenciesUseCase
            } returns getCurrenciesUseCase

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
        fun `saves the stored copy with the drafted source and clears the draft`() = runTest {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState(bookId = 42).copy(
                whereYouGotItDraft = buildWhereYouGotItDraft(source = WhereYouGotItSource.Preset(key = AcquisitionKind.GIFT.toKey())),
            )

            // ----- Act -----
            OnSaveWhereYouGotItAction().execute(
                dependencies = stubDependencies(this),
                scope = scope,
            )

            // ----- Assert -----
            savedCopies shouldBe listOf(
                BookCopy(
                    bookId = 42,
                    source = AcquisitionSource.Preset(kind = AcquisitionKind.GIFT),
                ),
            )

            stateFlow.value.whereYouGotItDraft.shouldBeNull()
        }

        @Test
        fun `saves trimmed custom text`() = runTest {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState(bookId = 42).copy(
                whereYouGotItDraft = buildWhereYouGotItDraft(source = WhereYouGotItSource.Custom(text = "  A street stall ")),
            )

            // ----- Act -----
            OnSaveWhereYouGotItAction().execute(
                dependencies = stubDependencies(this),
                scope = scope,
            )

            // ----- Assert -----
            savedCopies shouldBe listOf(
                BookCopy(
                    bookId = 42,
                    source = AcquisitionSource.Other(text = "A street stall"),
                ),
            )
        }

        @Test
        fun `saves an empty draft as no source so the stored source is cleared`() = runTest {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState(bookId = 42).copy(whereYouGotItDraft = buildWhereYouGotItDraft())

            // ----- Act -----
            OnSaveWhereYouGotItAction().execute(
                dependencies = stubDependencies(this),
                scope = scope,
            )

            // ----- Assert -----
            savedCopies shouldBe listOf(
                BookCopy(
                    bookId = 42,
                    source = null,
                ),
            )
        }

        @Test
        fun `sends MutationSucceededEvent after a successful save`() = runTest {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState(bookId = 42).copy(whereYouGotItDraft = buildWhereYouGotItDraft())

            // ----- Act -----
            OnSaveWhereYouGotItAction().execute(
                dependencies = stubDependencies(this),
                scope = scope,
            )

            // ----- Assert -----
            eventChannel.tryReceive().getOrNull().shouldBeInstanceOf<BookDetailEvent.MutationSucceededEvent>()
        }

        @Test
        fun `does nothing when there is no draft`() = runTest {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState(bookId = 42)

            // ----- Act -----
            OnSaveWhereYouGotItAction().execute(
                dependencies = stubDependencies(this),
                scope = scope,
            )

            // ----- Assert -----
            savedCopies.shouldBeEmpty()
            eventChannel.tryReceive().getOrNull().shouldBeNull()
        }

        @Test
        fun `shows a message and sends MutationFailedEvent when the save fails`() = runTest {
            // ----- Arrange -----
            coEvery {
                saveBookCopyUseCase(copy = any())
            } returns Result.failure(RuntimeException("save failed"))

            stateFlow.value = buildBookDetailUiState(bookId = 42).copy(whereYouGotItDraft = buildWhereYouGotItDraft())

            // ----- Act -----
            OnSaveWhereYouGotItAction().execute(
                dependencies = stubDependencies(this),
                scope = scope,
            )

            // ----- Assert -----
            verify(exactly = 1) {
                SnackBarManager.showSnackbar(title = "Couldn't save where you got it. Try again.")
            }

            eventChannel.tryReceive().getOrNull().shouldBeInstanceOf<BookDetailEvent.MutationFailedEvent>()
        }

        @Test
        fun `shows a message and sends MutationFailedEvent without saving when the stored copy cannot be read`() = runTest {
            // ----- Arrange -----
            coEvery {
                getBookCopyUseCase(bookId = 42)
            } returns Result.failure(RuntimeException("read failed"))

            stateFlow.value = buildBookDetailUiState(bookId = 42).copy(whereYouGotItDraft = buildWhereYouGotItDraft())

            // ----- Act -----
            OnSaveWhereYouGotItAction().execute(
                dependencies = stubDependencies(this),
                scope = scope,
            )

            // ----- Assert -----
            savedCopies.shouldBeEmpty()
            eventChannel.tryReceive().getOrNull().shouldBeInstanceOf<BookDetailEvent.MutationFailedEvent>()
        }

        @Test
        fun `saves the drafted price with its currency alongside the source`() = runTest {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState(bookId = 42).copy(
                whereYouGotItDraft = buildWhereYouGotItDraft(
                    source = WhereYouGotItSource.Custom(text = "A street stall"),
                    amountText = "14,5",
                    currencyCode = EURO.code,
                ),
            )

            // ----- Act -----
            OnSaveWhereYouGotItAction().execute(
                dependencies = stubDependencies(this),
                scope = scope,
            )

            // ----- Assert -----
            savedCopies shouldBe listOf(
                BookCopy(
                    bookId = 42,
                    source = AcquisitionSource.Other(text = "A street stall"),
                    price = PricePaid(
                        amountThousandths = 14_500,
                        currencyCode = EURO.code,
                    ),
                ),
            )
        }

        @Test
        fun `saves a price without a source`() = runTest {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState(bookId = 42).copy(
                whereYouGotItDraft = buildWhereYouGotItDraft(
                    amountText = "1450",
                    currencyCode = YEN.code,
                ),
            )

            // ----- Act -----
            OnSaveWhereYouGotItAction().execute(
                dependencies = stubDependencies(this),
                scope = scope,
            )

            // ----- Assert -----
            savedCopies shouldBe listOf(
                BookCopy(
                    bookId = 42,
                    price = PricePaid(
                        amountThousandths = 1_450_000,
                        currencyCode = YEN.code,
                    ),
                ),
            )
        }

        @Test
        fun `clears the stored price when the amount is emptied`() = runTest {
            // ----- Arrange -----
            coEvery {
                getBookCopyUseCase(bookId = 42)
            } returns Result.success(
                BookCopy(
                    bookId = 42,
                    price = PricePaid(
                        amountThousandths = 14_500,
                        currencyCode = EURO.code,
                    ),
                ),
            )

            stateFlow.value = buildBookDetailUiState(bookId = 42).copy(
                whereYouGotItDraft = buildWhereYouGotItDraft(amountText = ""),
            )

            // ----- Act -----
            OnSaveWhereYouGotItAction().execute(
                dependencies = stubDependencies(this),
                scope = scope,
            )

            // ----- Assert -----
            savedCopies shouldBe listOf(BookCopy(bookId = 42))
        }

        @Test
        fun `saves nothing and keeps the draft open while the amount has an error`() = runTest {
            // ----- Arrange -----
            val draft = buildWhereYouGotItDraft(
                amountText = "abc",
                amountError = "Enter a price like 14.50.",
            )

            stateFlow.value = buildBookDetailUiState(bookId = 42).copy(whereYouGotItDraft = draft)

            // ----- Act -----
            OnSaveWhereYouGotItAction().execute(
                dependencies = stubDependencies(this),
                scope = scope,
            )

            // ----- Assert -----
            savedCopies.shouldBeEmpty()
            stateFlow.value.whereYouGotItDraft shouldBe draft
            eventChannel.tryReceive().getOrNull().shouldBeNull()
        }
    }
}
