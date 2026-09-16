package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.uibinding.release.UnreleasedBadgeStyle
import nl.rhaydus.softcover.core.uibinding.release.toUnreleasedBadgeUiModel
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

private val RELEASE_DATE = LocalDate(
    2026,
    9,
    20,
)

class UnreleasedBadgeCollectorTest {
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
        isUnreleased: Boolean,
        effectiveReleaseDate: LocalDate?,
    ): Book = mockk {
        every {
            this@mockk.isUnreleased
        } returns isUnreleased

        every {
            this@mockk.effectiveReleaseDate
        } returns effectiveReleaseDate
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `sets unreleasedBadge to a Prominent badge when the book is unreleased with a release date`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val book = stubBook(
                    isUnreleased = true,
                    effectiveReleaseDate = RELEASE_DATE,
                )
                stateFlow.value = BookDetailUiState(book = book)
                val collector = UnreleasedBadgeCollector()

                // ----- Act -----
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Assert -----
                stateFlow.value.unreleasedBadge shouldBe RELEASE_DATE.toUnreleasedBadgeUiModel(UnreleasedBadgeStyle.Prominent)
                job.cancel()
            }

        @Test
        fun `unreleasedBadge is null for a released book`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val book = stubBook(
                isUnreleased = false,
                effectiveReleaseDate = RELEASE_DATE,
            )
            stateFlow.value = BookDetailUiState(book = book)
            val collector = UnreleasedBadgeCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            stateFlow.value.unreleasedBadge shouldBe null
            job.cancel()
        }

        @Test
        fun `unreleasedBadge is null when the book is unreleased but has no release date`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val book = stubBook(
                isUnreleased = true,
                effectiveReleaseDate = null,
            )
            stateFlow.value = BookDetailUiState(book = book)
            val collector = UnreleasedBadgeCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            stateFlow.value.unreleasedBadge shouldBe null
            job.cancel()
        }

        @Test
        fun `unreleasedBadge is null when state book is null`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = BookDetailUiState(book = null)
            val collector = UnreleasedBadgeCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            stateFlow.value.unreleasedBadge shouldBe null
            job.cancel()
        }
    }
}
