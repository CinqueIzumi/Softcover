package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.callout.CalloutActionUiModel
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.core.domain.model.UserBook
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
        stateFlow = MutableStateFlow(BookDetailUiState())
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
    ): Book {
        val userBook = mockk<UserBook> {
            every {
                editionId
            } returns shelvedEditionId

            every {
                getFallbackDateString(style = any())
            } returns "FALLBACK"
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
        fun `sets both callouts for a shelved book whose edition differs from the scanned one`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                stateFlow.value = BookDetailUiState(
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
            stateFlow.value = BookDetailUiState(book = null)
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
            stateFlow.value = BookDetailUiState(
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
        fun `scanEditionCallout switches to Busy while the scanned edition is updating`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                stateFlow.value = BookDetailUiState(
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
                stateFlow.value.scanEditionCallout.shouldNotBeNull().action.shouldBeInstanceOf<CalloutActionUiModel.Button>()

                // ----- Act -----
                stateFlow.value = stateFlow.value.copy(isUpdatingScannedEdition = true)

                // ----- Assert -----
                stateFlow.value.scanEditionCallout.shouldNotBeNull().action.shouldBeInstanceOf<CalloutActionUiModel.Busy>()
                job.cancel()
            }
    }
}
