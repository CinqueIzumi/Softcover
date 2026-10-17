package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.core.domain.model.DateStyle
import nl.rhaydus.softcover.core.domain.model.UserBook
import nl.rhaydus.softcover.core.domain.model.UserBookRead
import nl.rhaydus.softcover.feature.book_detail.presentation.buildBookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class YourReadsCollectorTest {
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

    private fun read(
        id: Int,
        startedAt: String? = null,
    ): UserBookRead = UserBookRead(
        id = id,
        currentPage = null,
        currentSeconds = null,
        progress = 0f,
        startedAt = startedAt,
        finishedAt = null,
    )

    private fun stubBook(
        status: BookStatus = BookStatus.Reading,
        reads: List<UserBookRead>,
    ): Book {
        val userBook = UserBook(
            id = 1,
            status = status,
            dateAdded = "2026-01-01",
            createdAt = null,
            privacySettingId = 1,
            reviewHasSpoilers = false,
            editionId = null,
            lastReadDate = null,
            rating = null,
            referrerUserId = null,
            reviewedAt = null,
            updatedAt = null,
            journals = emptyList(),
            reads = reads,
        )

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
        fun `sets yourReads from the book and date style`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(
                book = stubBook(reads = listOf(read(
                    id = 1,
                    startedAt = "2026-01-05",),
                ),),
                dateStyle = DateStyle.YEAR_MONTH_DAY,
            )
            val collector = YourReadsCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            val row = stateFlow.value.yourReads.shouldNotBeNull().rows.single()
            row.title shouldBe "Reading now"
            row.supporting shouldBe "Started 2026/01/05"
            job.cancel()
        }

        @Test
        fun `yourReads is null when there is no book`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(book = null)
            val collector = YourReadsCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            stateFlow.value.yourReads.shouldBeNull()
            job.cancel()
        }

        @Test
        fun `updates yourReads when the reads of the book change`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(book = stubBook(reads = listOf(read(id = 1))))
            val collector = YourReadsCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }
            stateFlow.value.yourReads.shouldNotBeNull().rows.size shouldBe 1

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(
                book = stubBook(
                    reads = listOf(
                        read(id = 1),
                        read(id = 2),
                    ),
                ),
            )

            // ----- Assert -----
            stateFlow.value.yourReads.shouldNotBeNull().rows.map { it.key } shouldBe listOf(2, 1)
            job.cancel()
        }

        @Test
        fun `reformats yourReads when the date style changes`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(
                book = stubBook(reads = listOf(read(
                    id = 1,
                    startedAt = "2026-01-05",),
                ),),
                dateStyle = DateStyle.YEAR_MONTH_DAY,
            )
            val collector = YourReadsCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(dateStyle = DateStyle.DAY_MONTH_YEAR)

            // ----- Assert -----
            stateFlow.value.yourReads.shouldNotBeNull().rows.single().supporting shouldBe "Started 05/01/2026"
            job.cancel()
        }
    }
}
