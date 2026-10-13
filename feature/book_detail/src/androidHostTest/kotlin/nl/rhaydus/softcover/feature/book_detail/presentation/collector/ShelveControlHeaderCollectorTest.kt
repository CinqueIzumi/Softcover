package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.header.SectionHeaderTone
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.book_detail.presentation.buildBookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ShelveControlHeaderCollectorTest {
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

    private fun stubBook(id: Int): Book = mockk {
        every {
            this@mockk.id
        } returns id
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `sets shelveControlHeader to the default Inline header when there is no failed mutation`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                stateFlow.value = buildBookDetailUiState().copy(
                    book = stubBook(id = 1),
                    failedMutationBookIds = persistentSetOf(),
                )
                val collector = ShelveControlHeaderCollector()

                // ----- Act -----
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Assert -----
                val header = stateFlow.value.shelveControlHeader
                header.shouldBeInstanceOf<SectionHeaderUiModel.Inline>().tone shouldNotBe SectionHeaderTone.Error
                job.cancel()
            }

        @Test
        fun `sets shelveControlHeader to an Error-toned retry header when the book's last mutation failed`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                stateFlow.value = buildBookDetailUiState().copy(
                    book = stubBook(id = 1),
                    failedMutationBookIds = persistentSetOf(1),
                )
                val collector = ShelveControlHeaderCollector()

                // ----- Act -----
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Assert -----
                val header = stateFlow.value.shelveControlHeader
                header.shouldBeInstanceOf<SectionHeaderUiModel.Inline>().tone shouldBe SectionHeaderTone.Error
                job.cancel()
            }

        @Test
        fun `sets shelveControlHeader to the default Inline header when a different book's mutation failed`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                stateFlow.value = buildBookDetailUiState().copy(
                    book = stubBook(id = 1),
                    failedMutationBookIds = persistentSetOf(2),
                )
                val collector = ShelveControlHeaderCollector()

                // ----- Act -----
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Assert -----
                val header = stateFlow.value.shelveControlHeader
                header.shouldBeInstanceOf<SectionHeaderUiModel.Inline>().tone shouldNotBe SectionHeaderTone.Error
                job.cancel()
            }

        @Test
        fun `sets shelveControlHeader to the default Inline header when state book is null`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                stateFlow.value = buildBookDetailUiState().copy(
                    book = null,
                    failedMutationBookIds = persistentSetOf(1),
                )
                val collector = ShelveControlHeaderCollector()

                // ----- Act -----
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Assert -----
                val header = stateFlow.value.shelveControlHeader
                header.shouldBeInstanceOf<SectionHeaderUiModel.Inline>().tone shouldNotBe SectionHeaderTone.Error
                job.cancel()
            }
    }
}
