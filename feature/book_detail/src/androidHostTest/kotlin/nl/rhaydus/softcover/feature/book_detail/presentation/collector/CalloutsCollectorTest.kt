package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.callout.CalloutActionUiModel
import nl.rhaydus.softcover.core.corpus.domain.usecase.ObserveDidNotFinishReasonsUseCase
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.core.domain.model.DidNotFinishReason
import nl.rhaydus.softcover.core.domain.model.DidNotFinishReasons
import nl.rhaydus.softcover.core.domain.model.UserBook
import nl.rhaydus.softcover.feature.book_detail.presentation.buildBookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class CalloutsCollectorTest {
    private lateinit var dependencies: BookDetailDependencies
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        stateFlow = MutableStateFlow(buildBookDetailUiState())

        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(BookDetailLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        dependencies = mockk(relaxed = true)
    }

    private fun stubBook(
        status: BookStatus,
        shelvedEditionId: Int?,
        readDate: String? = null,
    ): Book {
        val userBook = mockk<UserBook> {
            every {
                editionId
            } returns shelvedEditionId

            every {
                getFallbackDateString(style = any())
            } returns "FALLBACK"

            every {
                getReadDateString(style = any())
            } returns readDate

            every {
                getDnfDateString(style = any())
            } returns null
        }

        return mockk<Book> {
            every {
                this@mockk.status
            } returns status

            every {
                this@mockk.userBook
            } returns userBook
        }
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `sets both callouts for a shelved book whose edition differs from the scanned one`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(
                book = stubBook(
                    status = BookStatus.WantToRead,
                    shelvedEditionId = 10,
                ),
                scannedEditionId = 42,
            )

            val collector = CalloutsCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            stateFlow.value.statusCallout.shouldNotBeNull()
            stateFlow.value.scanEditionCallout.shouldNotBeNull()
            job.cancel()
        }

        @Test
        fun `both callouts are null when there is no book`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(book = null)
            val collector = CalloutsCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            stateFlow.value.statusCallout.shouldBeNull()
            stateFlow.value.scanEditionCallout.shouldBeNull()
            job.cancel()
        }

        @Test
        fun `scanEditionCallout disappears when the banner is dismissed`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(
                book = stubBook(
                    status = BookStatus.Reading,
                    shelvedEditionId = 10,
                ),
                scannedEditionId = 42,
            )

            val collector = CalloutsCollector()

            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(scannedEditionBannerDismissed = true)

            // ----- Assert -----
            stateFlow.value.scanEditionCallout.shouldBeNull()
            stateFlow.value.statusCallout.shouldBeNull()
            job.cancel()
        }

        @Test
        fun `scanEditionCallout switches to Busy while the scanned edition is updating`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(
                book = stubBook(
                    status = BookStatus.Reading,
                    shelvedEditionId = 10,
                ),
                scannedEditionId = 42,
            )

            val collector = CalloutsCollector()

            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            stateFlow.value.scanEditionCallout.shouldNotBeNull().callToAction
                .shouldBeInstanceOf<CalloutActionUiModel.Button>()

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(isUpdatingScannedEdition = true)

            // ----- Assert -----
            stateFlow.value.scanEditionCallout.shouldNotBeNull().callToAction
                .shouldBeInstanceOf<CalloutActionUiModel.Busy>()

            job.cancel()
        }

        @Test
        fun `sets shelveReadDate to the formatted read date of the user book`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(
                book = stubBook(
                    status = BookStatus.Read,
                    shelvedEditionId = 10,
                    readDate = "12/03/2026",
                ),
            )

            val collector = CalloutsCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            stateFlow.value.shelveReadDate shouldBe "12/03/2026"
            job.cancel()
        }

        @Test
        fun `shelveReadDate is null when the user book has no read date`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(
                book = stubBook(
                    status = BookStatus.Reading,
                    shelvedEditionId = 10,
                    readDate = null,
                ),
            )

            val collector = CalloutsCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            stateFlow.value.shelveReadDate.shouldBeNull()
            job.cancel()
        }
    }

    @Nested
    inner class StoredReasons {
        private val saved = DidNotFinishReasons(
            bookId = 42,
            reasons = setOf(DidNotFinishReason.TooSlow),
            ownWords = "The middle dragged",
        )

        private fun stubObservedReasons(source: Flow<DidNotFinishReasons> = MutableStateFlow(saved)) {
            val observeUseCase = mockk<ObserveDidNotFinishReasonsUseCase> {
                every {
                    this@mockk(bookId = any())
                } returns source
            }

            every {
                dependencies.observeDidNotFinishReasonsUseCase
            } returns observeUseCase
        }

        private fun detailLines(): List<String>? = stateFlow.value.statusCallout?.callToAction
            .shouldBeInstanceOf<CalloutActionUiModel.Detail>().lines

        @Test
        fun `shows the saved reasons while the status is Did not finish`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stubObservedReasons()

            stateFlow.value = buildBookDetailUiState(bookId = 42).copy(
                book = stubBook(
                    status = BookStatus.DidNotFinish,
                    shelvedEditionId = 10,
                ),
            )

            // ----- Act -----
            val job = launch {
                CalloutsCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            detailLines() shouldBe listOf("Too slow", "The middle dragged")
            job.cancel()
        }

        @Test
        fun `hides the reasons when the status moves away and shows them again on return`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stubObservedReasons()

            val dnfBook = stubBook(
                status = BookStatus.DidNotFinish,
                shelvedEditionId = 10,
            )

            stateFlow.value = buildBookDetailUiState(bookId = 42).copy(book = dnfBook)

            val job = launch {
                CalloutsCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(
                book = stubBook(
                    status = BookStatus.Read,
                    shelvedEditionId = 10,
                ),
            )

            val whileRead = stateFlow.value.statusCallout?.callToAction
            stateFlow.value = stateFlow.value.copy(book = dnfBook)

            // ----- Assert -----
            whileRead.shouldBeNull()
            detailLines() shouldBe listOf("Too slow", "The middle dragged")
            job.cancel()
        }

        @Test
        fun `writes no status callout until the reasons of a Did not finish book have loaded`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val loading = MutableSharedFlow<DidNotFinishReasons>()
            stubObservedReasons(source = loading)

            stateFlow.value = buildBookDetailUiState(bookId = 42).copy(
                book = stubBook(
                    status = BookStatus.DidNotFinish,
                    shelvedEditionId = 10,
                ),
            )

            val job = launch {
                CalloutsCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            val beforeLoad = stateFlow.value.statusCallout

            // ----- Act -----
            loading.emit(saved)

            // ----- Assert -----
            beforeLoad.shouldBeNull()
            detailLines() shouldBe listOf("Too slow", "The middle dragged")
            job.cancel()
        }

        @Test
        fun `keeps the Read callout until the reasons load when the status becomes Did not finish`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val loading = MutableSharedFlow<DidNotFinishReasons>()
            stubObservedReasons(source = loading)

            stateFlow.value = buildBookDetailUiState(bookId = 42).copy(
                book = stubBook(
                    status = BookStatus.Read,
                    shelvedEditionId = 10,
                ),
            )

            val job = launch {
                CalloutsCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            val readEyebrow = stateFlow.value.statusCallout.shouldNotBeNull().eyebrow

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(
                book = stubBook(
                    status = BookStatus.DidNotFinish,
                    shelvedEditionId = 10,
                ),
            )

            val whileLoading = stateFlow.value.statusCallout
            loading.emit(saved)

            // ----- Assert -----
            whileLoading.shouldNotBeNull().eyebrow shouldBe readEyebrow
            whileLoading.callToAction.shouldBeNull()
            detailLines() shouldBe listOf("Too slow", "The middle dragged")
            job.cancel()
        }
    }
}
